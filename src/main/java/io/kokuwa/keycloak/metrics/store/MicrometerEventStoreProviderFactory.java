package io.kokuwa.keycloak.metrics.store;

import io.kokuwa.keycloak.metrics.CommunityProfiles;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.jpa.JpaEventStoreProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;


public class MicrometerEventStoreProviderFactory extends JpaEventStoreProviderFactory implements EnvironmentDependentProviderFactory {
    private static final Logger log = Logger.getLogger(MicrometerEventStoreProviderFactory.class);

    public static final String ID = "jpa"; // Override default event store provider

    @Override
    public EventStoreProvider create(KeycloakSession session) {
        JpaConnectionProvider connection = session.getProvider(JpaConnectionProvider.class);
        return new MicrometerEventStoreProvider(session, connection.getEntityManager());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isSupported() {
        log.info("MicrometerEventStore is supported:"+ CommunityProfiles.isEventsMetricsEnabled());
        return CommunityProfiles.isEventsMetricsEnabled();
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        log.info("MicrometerEventStore is supported:"+ CommunityProfiles.isEventsMetricsEnabled());
        return CommunityProfiles.isEventsMetricsEnabled();
    }
}
