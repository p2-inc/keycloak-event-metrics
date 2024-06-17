package io.kokuwa.keycloak.metrics.event;

import io.kokuwa.keycloak.metrics.CommunityProfiles;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * Factory for {@link MetricsEventListener}.
 *
 * @author Stephan Schnabel
 */
public class MetricsEventListenerFactory implements EventListenerProviderFactory, EnvironmentDependentProviderFactory {

    private static final Logger log = Logger.getLogger(MetricsEventListenerFactory.class);
    private boolean replaceIds;

    @Override
    public String getId() {
        return "metrics-listener";
    }

    @Override
    public void init(Scope config) {
        replaceIds = "true".equals(System.getenv().getOrDefault("KC_METRICS_EVENT_REPLACE_IDS", "true"));
        log.info(replaceIds ? "Configured with model names." : "Configured with model ids.");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new MetricsEventListener(replaceIds, session);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isSupported() {
        log.info("MetricsEventListenerFactory is supported:" + !CommunityProfiles.isEventsMetricsEnabled());
        return !CommunityProfiles.isEventsMetricsEnabled();
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        log.info("MetricsEventListenerFactory is supported:" + !CommunityProfiles.isEventsMetricsEnabled());
        return !CommunityProfiles.isEventsMetricsEnabled();
    }
}
