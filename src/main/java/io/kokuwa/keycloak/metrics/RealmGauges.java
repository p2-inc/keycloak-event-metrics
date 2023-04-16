package io.kokuwa.keycloak.metrics;

import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class RealmGauges implements GaugeHolder {

  private final MeterRegistry registry;
  private final MicrometerEventConfig config;
  private final String realmId;
  private final String realmName;
  private final List<Meter.Id> gauges;

  public RealmGauges(MeterRegistry registry, MicrometerEventConfig config, RealmModel realm) {
    this.registry = registry;
    this.config = config;
    this.realmId = realm.getId();
    this.realmName = realm.getName();
    this.gauges = Lists.newArrayList();
  }

  @Override
  public void setup(final KeycloakSessionFactory factory) {
    // keycloak_users = total number of users
    Gauge keycloakUsers =
        Gauge.builder(
                "keycloak_users",
                () -> {
                  return KeycloakModelUtils.runJobInTransactionWithResult(
                      factory,
                      (KeycloakSession session) -> {
                        RealmModel realm = session.realms().getRealmByName(realmName);
                        return session.users().getUsersCount(realm);
                      });
                })
            .tags(tags("realm", config.replaceIds() ? realmName : realmId))
            .register(registry);
    gauges.add(keycloakUsers.getId());

    // keycloak_clients = total number of clients
    Gauge keycloakClients =
        Gauge.builder(
                "keycloak_clients",
                () -> {
                  return KeycloakModelUtils.runJobInTransactionWithResult(
                      factory,
                      (KeycloakSession session) -> {
                        RealmModel realm = session.realms().getRealmByName(realmName);
                        return realm.getClientsCount();
                      });
                })
            .tags(tags("realm", config.replaceIds() ? realmName : realmId))
            .register(registry);
    gauges.add(keycloakClients.getId());
  }

  @Override
  public void remove() {
    gauges.stream().forEach(id -> registry.remove(id));
  }
}
