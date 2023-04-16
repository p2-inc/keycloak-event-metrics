package io.kokuwa.keycloak.metrics;

import com.google.common.collect.Maps;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import javax.enterprise.inject.spi.CDI;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderEvent;

/**
 * Factory for {@link MicrometerEventListener}, uses {@link MeterRegistry} from CDI.
 *
 * @author Stephan Schnabel
 */
@JBossLog
public class MicrometerEventListenerFactory implements EventListenerProviderFactory {

  private static final String PROVIDER_ID = "ext-metrics-listener";
  private static final int INTERVAL = 60 * 1000; // 1 MINUTE

  private static final Logger log = Logger.getLogger(MicrometerEventListener.class);
  private MeterRegistry registry;
  private MicrometerEventConfig config;
  private Map<String, RealmGauges> realmGauges;
  private Map<String, ClientGauges> clientGauges;

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public void init(Scope scopeConfig) {
    config = MicrometerEventConfig.getConfig();
    log.infof("Configuration: %s", config);
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    registry = CDI.current().select(MeterRegistry.class).get();

    if (config.dataCount()) {
      realmGauges = Maps.newHashMap();
      clientGauges = Maps.newHashMap();
      KeycloakModelUtils.runJobInTransaction(
          factory,
          session -> session.realms().getRealmsStream().forEach(r -> realmAddGauges(r, factory)));

      factory.register(
          (ProviderEvent event) -> {
            if (event instanceof RealmModel.RealmPostCreateEvent) {
              log.debug("RealmPostCreateEvent");
              realmPostCreate((RealmModel.RealmPostCreateEvent) event, factory);
            } else if (event instanceof RealmModel.RealmRemovedEvent) {
              log.debug("RealmRemovedEvent");
              realmRemoved((RealmModel.RealmRemovedEvent) event);
            } else if (event instanceof ClientModel.ClientCreationEvent) {
              log.debug("ClientCreationEvent");
              clientCreate((ClientModel.ClientCreationEvent) event, factory);
            } else if (event instanceof ClientModel.ClientRemovedEvent) {
              log.debug("ClientRemovedEvent");
              clientRemoved((ClientModel.ClientRemovedEvent) event);
            }
          });
    }
  }

  private void realmPostCreate(
      RealmModel.RealmPostCreateEvent event, KeycloakSessionFactory factory) {
    realmAddGauges(event.getCreatedRealm(), factory);
  }

  private void realmAddGauges(RealmModel r, KeycloakSessionFactory factory) {
    RealmGauges rg = realmGauges.get(r.getId());
    if (rg == null) {
      rg = new RealmGauges(registry, config, r);
      rg.setup(factory);
      realmGauges.put(r.getId(), rg);
      r.getClientsStream().forEach(c -> clientAddGauges(c, factory));
    }
  }

  private void realmRemoved(RealmModel.RealmRemovedEvent event) {
    RealmGauges rg = realmGauges.remove(event.getRealm().getId());
    if (rg != null) {
      rg.remove();
    }
  }

  private void clientCreate(ClientModel.ClientCreationEvent event, KeycloakSessionFactory factory) {
    clientAddGauges(event.getCreatedClient(), factory);
  }

  private void clientAddGauges(ClientModel c, KeycloakSessionFactory factory) {
    ClientGauges cg = clientGauges.get(c.getId());
    if (cg == null) {
      cg = new ClientGauges(registry, config, c.getRealm(), c);
      cg.setup(factory);
      clientGauges.put(c.getId(), cg);
    }
  }

  private void clientRemoved(ClientModel.ClientRemovedEvent event) {
    ClientGauges cg = clientGauges.remove(event.getClient().getId());
    if (cg != null) {
      cg.remove();
    }
  }

  @Override
  public EventListenerProvider create(KeycloakSession session) {
    return new MicrometerEventListener(registry, session, config);
  }

  @Override
  public void close() {}
}
