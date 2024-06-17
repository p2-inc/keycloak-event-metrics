package io.kokuwa.keycloak.metrics;

public class CommunityProfiles {
    private static final String ENV_EVENTS_METRICS_ENABLED = "KC_COMMUNITY_EVENTS_METRICS_ENABLED";
    private static final String PROP_EVENTS_METRICS_ENABLED = "kc.community.events.metrics.enabled";

    private static final boolean isEventsMetricsEnabled;

    static {
        isEventsMetricsEnabled =
                Boolean.parseBoolean(System.getenv(ENV_EVENTS_METRICS_ENABLED))
                        || Boolean.parseBoolean(System.getProperty(PROP_EVENTS_METRICS_ENABLED));
    }

    private CommunityProfiles() {
    }

    public static boolean isEventsMetricsEnabled() {
        return isEventsMetricsEnabled;
    }
}
