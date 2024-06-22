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
    public static final String ID = "metrics-listener";

    @Override
    public String getId() {
        return ID;
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


    //to avoid double metric registration
    @Override
    public boolean isSupported() {
        var defaultEventsMetricsDisabled = !CommunityProfiles.isEventsMetricsEnabled();
        log.info("MetricsEventListenerFactory is enabled:" + defaultEventsMetricsDisabled);
        return defaultEventsMetricsDisabled;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        var defaultEventsMetricsDisabled = !CommunityProfiles.isEventsMetricsEnabled();
        log.info("MetricsEventListenerFactory is enabled:" + defaultEventsMetricsDisabled);
        return defaultEventsMetricsDisabled;
    }
}
