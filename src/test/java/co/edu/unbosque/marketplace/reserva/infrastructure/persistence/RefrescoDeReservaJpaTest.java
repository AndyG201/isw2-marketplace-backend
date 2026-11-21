package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import jakarta.persistence.EntityManager;

class RefrescoDeReservaJpaTest {

    @Test
    void refrescarRelaeLaReservaDesdeLaBaseDeDatos() {
        EntityManager entityManager = mock(EntityManager.class);
        Reserva reserva = new Reserva();

        new RefrescoDeReservaJpa(entityManager).refrescar(reserva);

        verify(entityManager).refresh(reserva);
    }
}
