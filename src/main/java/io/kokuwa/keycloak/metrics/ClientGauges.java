package io.kokuwa.keycloak.metrics;

import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class ClientGauges implements GaugeHolder {

  private final MeterRegistry registry;
  private final MicrometerEventConfig config;
  private final String realmId;
  private final String realmName;
  private final String clientId;
  private final String clientName;
  private final List<Meter.Id> gauges;

  public ClientGauges(
      MeterRegistry registry, MicrometerEventConfig config, RealmModel realm, ClientModel client) {
    this.registry = registry;
    this.config = config;
    this.realmId = realm.getId();
    this.realmName = realm.getName();
    this.clientId = client.getId();
    this.clientName = client.getClientId();
    this.gauges = Lists.newArrayList();
  }

  @Override
  public void setup(final KeycloakSessionFactory factory) {
    // keycloak_active_user_sessions
    Gauge keycloakActiveUserSessions =
        Gauge.builder(
                "keycloak_active_user_sessions",
                () -> {
                  return KeycloakModelUtils.runJobInTransactionWithResult(
                      factory,
                      (KeycloakSession session) -> {
                        RealmModel realm = session.realms().getRealmByName(realmName);
                        ClientModel client = realm.getClientByClientId(clientName);
                        return session.sessions().getActiveUserSessions(realm, client);
                      });
                })
            .tags(
                tags(
                    "realm", config.replaceIds() ? realmName : realmId,
                    "client", config.replaceIds() ? clientName : clientId))
            .register(registry);
    gauges.add(keycloakActiveUserSessions.getId());

    // keycloak_active_client_sessions
    Gauge keycloakActiveClientSessions =
        Gauge.builder(
                "keycloak_active_client_sessions",
                () -> {
                  return KeycloakModelUtils.runJobInTransactionWithResult(
                      factory,
                      (KeycloakSession session) -> {
                        RealmModel realm = session.realms().getRealmByName(realmName);
                        return session
                            .sessions()
                            .getActiveClientSessionStats(realm, false)
                            .get(clientId);
                      });
                })
            .tags(
                tags(
                    "realm", config.replaceIds() ? realmName : realmId,
                    "client", config.replaceIds() ? clientName : clientId))
            .register(registry);
    gauges.add(keycloakActiveClientSessions.getId());

    // keycloak_offline_sessions
    Gauge keycloakOfflineSessions =
        Gauge.builder(
                "keycloak_offline_sessions",
                () -> {
                  return KeycloakModelUtils.runJobInTransactionWithResult(
                      factory,
                      (KeycloakSession session) -> {
                        RealmModel realm = session.realms().getRealmByName(realmName);
                        ClientModel client = realm.getClientByClientId(clientName);
                        return session.sessions().getOfflineSessionsCount(realm, client);
                      });
                })
            .tags(
                tags(
                    "realm", config.replaceIds() ? realmName : realmId,
                    "client", config.replaceIds() ? clientName : clientId))
            .register(registry);
    gauges.add(keycloakOfflineSessions.getId());
  }

  @Override
  public void remove() {
    gauges.stream().forEach(id -> registry.remove(id));
  }
}
