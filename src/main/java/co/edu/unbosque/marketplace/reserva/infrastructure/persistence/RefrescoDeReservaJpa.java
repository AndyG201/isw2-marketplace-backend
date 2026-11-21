package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reserva.application.port.RefrescoDeReserva;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import jakarta.persistence.EntityManager;

@Component
public class RefrescoDeReservaJpa implements RefrescoDeReserva {

    private final EntityManager entityManager;

    public RefrescoDeReservaJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void refrescar(Reserva reserva) {
        entityManager.refresh(reserva);
    }
}
