package io.kokuwa.keycloak.metrics.store;

import jakarta.persistence.EntityManager;
import org.keycloak.events.jpa.JpaEventStoreProvider;
import org.keycloak.models.KeycloakSession;

public class MicrometerEventStoreProvider extends JpaEventStoreProvider {

    public MicrometerEventStoreProvider(KeycloakSession session, EntityManager em) {
        super(session, em);
    }
}