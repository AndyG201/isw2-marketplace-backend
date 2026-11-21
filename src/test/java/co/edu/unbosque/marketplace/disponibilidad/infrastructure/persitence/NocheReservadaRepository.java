package co.edu.unbosque.marketplace.disponibilidad.infrastructure.persitence;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import co.edu.unbosque.marketplace.disponibilidad.domain.NocheReservada;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.NocheReservadaRepository;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class NocheReservadaRepositoryTest {

    @Autowired
    private NocheReservadaRepository repository;

    @Test
    void guardarYConsultarNocheReservada() {
        UUID idNoche = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(2026, 11, 20);

        NocheReservada noche = new NocheReservada(
                idNoche,
                idAlojamiento,
                fecha
        );

        NocheReservada resultado = repository.saveAndFlush(noche);

        assertTrue(repository.findById(idNoche).isPresent());
        assertFalse(repository.findById(UUID.randomUUID()).isPresent());
    }
}
