package io.kokuwa.keycloak.metrics.store;

import io.kokuwa.keycloak.metrics.CommunityProfiles;
import io.kokuwa.keycloak.metrics.event.MetricsEventListenerFactory;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.jpa.JpaEventStoreProviderFactory;
import org.keycloak.models.KeycloakSession;

import java.util.stream.Collectors;

public class MicrometerEventStoreProviderFactory extends JpaEventStoreProviderFactory {
    private static final Logger log = Logger.getLogger(MicrometerEventStoreProviderFactory.class);

    public static final String ID = "jpa"; // Override default event store provider
    private boolean replaceIds;

    @Override
    public EventStoreProvider create(KeycloakSession session) {
        //removeMetricsEventsListenerIfExists(session);

        JpaConnectionProvider connection = session.getProvider(JpaConnectionProvider.class);
        return new MicrometerEventStoreProvider(session, connection.getEntityManager(), replaceIds);
    }

    public void init(Config.Scope config) {
        replaceIds = "true".equals(System.getenv().getOrDefault("KC_METRICS_EVENT_REPLACE_IDS", "true"));
        log.info(replaceIds ? "Configured with model names." : "Configured with model ids.");
    }

    @Override
    public String getId() {
        return ID;
    }

    // we need to remove 'metrics-listener' if already exists in realm, and KC_COMMUNITY_EVENTS_METRICS_ENABLED=true otherwise the keycloak_1  | 2024-06-22 06:37:29,059 ERROR [org.keycloak.services] (executor-thread-3) KC-SERVICES0083: Event listener 'metrics-listener' registered, but provider not found
    //Todo: This approach gives Unique index or primary key violation: "PUBLIC.PRIMARY_KEY_C38 ON PUBLIC.REALM_EVENTS_LISTENERS(REALM_ID, ""VALUE"") VALUES ( /* key:4 */ '32497f09-6079-42d3-8f56-9e4654b53e5e', 'jboss-logging')"; SQL statement:
    //Todo: Approach is same on AdminRealmEvents.updateRealmEventsConfig. xgp any idea why?
    private static void removeMetricsEventsListenerIfExists(KeycloakSession session) {
        var eventsMetricsEnabled = CommunityProfiles.isEventsMetricsEnabled();
        if (eventsMetricsEnabled) {
            var events = session.getContext().getRealm().getEventsListenersStream()
                    .filter(eventListener -> !MetricsEventListenerFactory.ID.equals(eventListener))
                    .collect(Collectors.toSet());

            session.getContext().getRealm().setEventsListeners(events);
        }
    }
}
