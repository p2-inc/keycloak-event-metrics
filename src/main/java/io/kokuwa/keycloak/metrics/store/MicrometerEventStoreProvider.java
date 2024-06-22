package io.kokuwa.keycloak.metrics.store;

import io.kokuwa.keycloak.metrics.CommunityProfiles;
import io.micrometer.core.instrument.Metrics;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.jpa.JpaEventStoreProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.util.Optional;

public class MicrometerEventStoreProvider extends JpaEventStoreProvider {
    private static final Logger log = Logger.getLogger(MicrometerEventStoreProvider.class);

    private final boolean replaceIds;
    private final KeycloakSession session;

    public MicrometerEventStoreProvider(KeycloakSession session,
                                        EntityManager em,
                                        boolean replaceIds) {
        super(session, em);
        this.replaceIds = replaceIds;
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        var isEventsEnable = CommunityProfiles.isEventsMetricsEnabled();
        if (isEventsEnable) {
            Metrics.counter("keycloak_event_user",
                            "realm", toBlank(replaceIds ? getRealmName(event.getRealmId()) : event.getRealmId()),
                            "type", toBlank(event.getType()),
                            "client", toBlank(event.getClientId()),
                            "error", toBlank(event.getError()))
                    .increment();
        }
        super.onEvent(event);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        var isEventsEnable = CommunityProfiles.isEventsMetricsEnabled();
        if (isEventsEnable) {
            Metrics.counter("keycloak_event_admin",
                            "realm", toBlank(replaceIds ? getRealmName(event.getRealmId()) : event.getRealmId()),
                            "resource", toBlank(event.getResourceType()),
                            "operation", toBlank(event.getOperationType()),
                            "error", toBlank(event.getError()))
                    .increment();
        }
        super.onEvent(event, includeRepresentation);
    }

    @Override
    public void close() {
    }

    private String getRealmName(String id) {
        return Optional.ofNullable(session.getContext()).map(KeycloakContext::getRealm)
                .filter(realm -> id == null || id.equals(realm.getId()))
                .or(() -> {
                    log.tracev("Context realm was empty with id {0}", id);
                    return Optional.ofNullable(id).map(session.realms()::getRealm);
                })
                .map(RealmModel::getName)
                .orElseGet(() -> {
                    log.warnv("Failed to find realm with id {0}", id);
                    return id;
                });
    }

    private String toBlank(Object value) {
        return value == null ? "" : value.toString();
    }
}