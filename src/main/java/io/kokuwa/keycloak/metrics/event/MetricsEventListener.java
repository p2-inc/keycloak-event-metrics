package io.kokuwa.keycloak.metrics.event;

import java.util.Optional;


import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

import io.micrometer.core.instrument.Metrics;
import io.kokuwa.keycloak.metrics.CommunityProfiles;
/**
 * Listener for {@link Event} and {@link AdminEvent}.
 *
 * @author Stephan Schnabel
 */
public class MetricsEventListener implements EventListenerProvider, AutoCloseable {

	private static final Logger log = Logger.getLogger(MetricsEventListener.class);
	private final boolean replaceIds;
	private final KeycloakSession session;

	MetricsEventListener(boolean replaceIds, KeycloakSession session) {
		this.replaceIds = replaceIds;
		this.session = session;
	}

	@Override
	public void onEvent(Event event) {
		Metrics.counter("keycloak_event_user",
				"realm", toBlank(replaceIds ? getRealmName(event.getRealmId()) : event.getRealmId()),
				"type", toBlank(event.getType()),
				"client", toBlank(event.getClientId()),
				"error", toBlank(event.getError()))
				.increment();
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		Metrics.counter("keycloak_event_admin",
				"realm", toBlank(replaceIds ? getRealmName(event.getRealmId()) : event.getRealmId()),
				"resource", toBlank(event.getResourceType()),
				"operation", toBlank(event.getOperationType()),
				"error", toBlank(event.getError()))
				.increment();
	}

	@Override
	public void close() {}

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
