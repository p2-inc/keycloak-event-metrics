package io.kokuwa.keycloak.metrics.store;

import io.kokuwa.keycloak.metrics.CommunityProfiles;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.jpa.JpaEventStoreProviderFactory;
import org.keycloak.models.KeycloakSession;

public class MicrometerEventStoreProviderFactory extends JpaEventStoreProviderFactory {
    private static final Logger log = Logger.getLogger(MicrometerEventStoreProviderFactory.class);

    public static final String ID = "jpa"; // Override default event store provider
    private boolean replaceIds;
    private boolean isEventsMetricsEnabled;

    @Override
    public EventStoreProvider create(KeycloakSession session) {
        JpaConnectionProvider connection = session.getProvider(JpaConnectionProvider.class);
        return new MicrometerEventStoreProvider(session, connection.getEntityManager(), replaceIds, isEventsMetricsEnabled);
    }

    public void init(Config.Scope config) {
        replaceIds = "true".equals(System.getenv().getOrDefault("KC_METRICS_EVENT_REPLACE_IDS", "true"));
        log.info(replaceIds ? "Configured with model names." : "Configured with model ids.");

        isEventsMetricsEnabled = CommunityProfiles.isEventsMetricsEnabled();
        log.info("Admin event metrics enabled: " + isEventsMetricsEnabled);
    }

    @Override
    public String getId() {
        return ID;
    }
}
