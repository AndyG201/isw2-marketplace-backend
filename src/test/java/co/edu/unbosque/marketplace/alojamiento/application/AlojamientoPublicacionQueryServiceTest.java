package co.edu.unbosque.marketplace.alojamiento.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
    private AlojamientoRepository alojamientoRepository;

    @InjectMocks
    private AlojamientoPublicacionQueryService servicio;

    private UUID idAlojamiento;
    private UUID idAnfitrion;

    @BeforeEach
    void setUp() {
        idAlojamiento = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
    }

    private Alojamiento crearAlojamiento(
            UUID id, UUID anfitrion, String ubicacion,
            String ciudad, String tipo, int capacidad) {

        Alojamiento alojamiento = new Alojamiento();

        alojamiento.setIdAlojamiento(id);
        alojamiento.setIdAnfitrion(anfitrion);
        alojamiento.setUbicacion(ubicacion);
        alojamiento.setCiudad(ciudad);
        alojamiento.setTipoAlojamiento(tipo);
        alojamiento.setCapacidad(capacidad);

        return alojamiento;
    }

    @Test
    void debeConsultarIdAnfitrionCorrectamente() {
        Alojamiento alojamiento = crearAlojamiento(
                idAlojamiento, idAnfitrion,
                "Calle 123", "Bogotá", "Apartamento", 4
        );

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        Optional<UUID> resultado =
                servicio.consultarIdAnfitrion(idAlojamiento);

        assertTrue(resultado.isPresent());
        assertEquals(idAnfitrion, resultado.get());

        verify(alojamientoRepository).findById(idAlojamiento);
    }

    @Test
    void debeRetornarVacioCuandoElAlojamientoNoExisteAlConsultarAnfitrion() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        Optional<UUID> resultado =
                servicio.consultarIdAnfitrion(idAlojamiento);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRetornarVacioCuandoElIdAlojamientoEsNuloAlConsultarAnfitrion() {
        Optional<UUID> resultado =
                servicio.consultarIdAnfitrion(null);

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void debeConsultarDatosParaPublicacionCorrectamente() {
        Alojamiento alojamiento = crearAlojamiento(
                idAlojamiento, idAnfitrion,
                "Calle 123", "Bogotá", "Apartamento", 4
        );

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        Optional<AlojamientoPublicacionInfo> resultado =
                servicio.consultarDatosParaPublicacion(idAlojamiento);

        assertTrue(resultado.isPresent());

        AlojamientoPublicacionInfo informacion = resultado.get();

        assertEquals(idAlojamiento, informacion.idAlojamiento());
        assertEquals(idAnfitrion, informacion.idAnfitrion());
        assertEquals("Calle 123", informacion.ubicacion());
        assertEquals("Bogotá", informacion.ciudad());
        assertEquals("Apartamento", informacion.tipoAlojamiento());
        assertEquals(4, informacion.capacidad());
    }

    @Test
    void debeRetornarVacioCuandoElAlojamientoNoExisteAlConsultarDatos() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        Optional<AlojamientoPublicacionInfo> resultado =
                servicio.consultarDatosParaPublicacion(idAlojamiento);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRetornarVacioCuandoElIdEsNuloAlConsultarDatos() {
        Optional<AlojamientoPublicacionInfo> resultado =
                servicio.consultarDatosParaPublicacion(null);

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void debeConsultarAlojamientosPorAnfitrion() {
        Alojamiento alojamiento1 = crearAlojamiento(
                idAlojamiento, idAnfitrion,
                "Calle 123", "Bogotá", "Apartamento", 4
        );

        Alojamiento alojamiento2 = crearAlojamiento(
                UUID.randomUUID(), idAnfitrion,
                "Carrera 15", "Medellín", "Casa", 6
        );

        when(alojamientoRepository.findByIdAnfitrion(idAnfitrion))
                .thenReturn(List.of(alojamiento1, alojamiento2));

        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorAnfitrion(idAnfitrion);

        assertEquals(2, resultado.size());

        assertEquals(idAlojamiento, resultado.get(0).idAlojamiento());
        assertEquals("Bogotá", resultado.get(0).ciudad());
        assertEquals("Apartamento", resultado.get(0).tipoAlojamiento());

        assertEquals("Medellín", resultado.get(1).ciudad());
        assertEquals("Casa", resultado.get(1).tipoAlojamiento());

        verify(alojamientoRepository).findByIdAnfitrion(idAnfitrion);
    }

    @Test
    void debeRetornarListaVaciaCuandoElAnfitrionNoTieneAlojamientos() {
        when(alojamientoRepository.findByIdAnfitrion(idAnfitrion))
                .thenReturn(List.of());

        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorAnfitrion(idAnfitrion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRetornarListaVaciaCuandoElIdAnfitrionEsNulo() {
        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorAnfitrion(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void debeConsultarAlojamientosPorIds() {
        Alojamiento alojamiento1 = crearAlojamiento(
                idAlojamiento, idAnfitrion,
                "Calle 123", "Bogotá", "Apartamento", 4
        );

        UUID segundoId = UUID.randomUUID();

        Alojamiento alojamiento2 = crearAlojamiento(
                segundoId, idAnfitrion,
                "Carrera 15", "Medellín", "Casa", 6
        );

        List<UUID> ids = List.of(idAlojamiento, segundoId);

        when(alojamientoRepository.findAllById(ids))
                .thenReturn(List.of(alojamiento1, alojamiento2));

        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorIdsAlojamiento(ids);

        assertEquals(2, resultado.size());

        assertEquals(idAlojamiento, resultado.get(0).idAlojamiento());
        assertEquals(idAnfitrion, resultado.get(0).idAnfitrion());
        assertEquals("Bogotá", resultado.get(0).ciudad());
        assertEquals(4, resultado.get(0).capacidad());

        assertEquals(segundoId, resultado.get(1).idAlojamiento());
        assertEquals("Medellín", resultado.get(1).ciudad());
        assertEquals(6, resultado.get(1).capacidad());

        verify(alojamientoRepository).findAllById(ids);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoEncuentraAlojamientosPorIds() {
        List<UUID> ids = List.of(idAlojamiento);

        when(alojamientoRepository.findAllById(ids))
                .thenReturn(List.of());

        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorIdsAlojamiento(ids);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRetornarListaVaciaCuandoLaColeccionDeIdsEsNula() {
        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorIdsAlojamiento(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void debeRetornarListaVaciaCuandoLaColeccionDeIdsEstaVacia() {
        List<AlojamientoPublicacionInfo> resultado =
                servicio.consultarPorIdsAlojamiento(List.of());

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(alojamientoRepository);
    }
}
