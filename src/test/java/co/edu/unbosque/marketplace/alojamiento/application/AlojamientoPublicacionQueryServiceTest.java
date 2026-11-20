package co.edu.unbosque.marketplace.alojamiento.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;

@ExtendWith(MockitoExtension.class)
class AlojamientoPublicacionQueryServiceTest {

    @Mock
    private AlojamientoRepository repositorio;

    @InjectMocks
    private AlojamientoPublicacionQueryService servicio;

    private Alojamiento alojamiento(UUID id, UUID anfitrion) {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setIdAlojamiento(id);
        alojamiento.setIdAnfitrion(anfitrion);
        alojamiento.setUbicacion("Calle 1");
        alojamiento.setCiudad("Bogotá");
        alojamiento.setTipoAlojamiento("Casa");
        alojamiento.setCapacidad(6);
        alojamiento.setHabitaciones(3);
        alojamiento.setCamas(4);
        alojamiento.setBanos(2);
        return alojamiento;
    }

    @Test
    void incluyeLaDistribucionEnTodasLasConsultas() {
        UUID id = UUID.randomUUID();
        UUID anfitrion = UUID.randomUUID();
        Alojamiento alojamiento = alojamiento(id, anfitrion);
        when(repositorio.findById(id)).thenReturn(Optional.of(alojamiento));
        when(repositorio.findByIdAnfitrion(anfitrion)).thenReturn(List.of(alojamiento));
        when(repositorio.findAllById(List.of(id))).thenReturn(List.of(alojamiento));

        AlojamientoPublicacionInfo esperado = new AlojamientoPublicacionInfo(
                id, anfitrion, "Calle 1", "Bogotá", "Casa", 6, 3, 4, 2);

        assertEquals(Optional.of(esperado), servicio.consultarDatosParaPublicacion(id));
        assertEquals(List.of(esperado), servicio.consultarPorAnfitrion(anfitrion));
        assertEquals(List.of(esperado), servicio.consultarPorIdsAlojamiento(List.of(id)));
    }
}
