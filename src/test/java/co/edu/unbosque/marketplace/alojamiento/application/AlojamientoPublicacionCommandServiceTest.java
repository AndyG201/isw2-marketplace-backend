package co.edu.unbosque.marketplace.alojamiento.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.ActualizarAlojamientoParaPublicacion;
import co.edu.unbosque.marketplace.alojamiento.contracts.CrearAlojamientoParaPublicacion;
import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;

@ExtendWith(MockitoExtension.class)
class AlojamientoPublicacionCommandServiceTest {

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @InjectMocks
    private AlojamientoPublicacionCommandService servicio;

    private UUID idAnfitrion;
    private UUID idAlojamiento;

    @BeforeEach
    void setUp() {
        idAnfitrion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
    }

    @Test
    void debeCrearAlojamientoCorrectamente() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion,
                        " Calle 123 ",
                        " Bogotá ",
                        " Apartamento ",
                        4
                );

        UUID resultado = servicio.crearAlojamiento(solicitud);

        assertNotNull(resultado);

        ArgumentCaptor<Alojamiento> captor =
                ArgumentCaptor.forClass(Alojamiento.class);

        verify(alojamientoRepository).save(captor.capture());

        Alojamiento guardado = captor.getValue();

        assertEquals(resultado, guardado.getIdAlojamiento());
        assertEquals(idAnfitrion, guardado.getIdAnfitrion());
        assertEquals("Calle 123", guardado.getUbicacion());
        assertEquals("Bogotá", guardado.getCiudad());
        assertEquals("Apartamento", guardado.getTipoAlojamiento());
        assertEquals(4, guardado.getCapacidad());
        assertNotNull(guardado.getFechaCreacion());
    }

    @Test
    void debeRechazarSolicitudDeCreacionNula() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(null)
        );

        assertEquals(
                "Debes proporcionar un anfitrión válido.",
                exception.getMessage()
        );

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarAnfitrionNulo() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        null, "Calle 123", "Bogotá", "Apartamento", 4
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(solicitud)
        );

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarDatosObligatoriosVaciosAlCrear() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion, " ", "Bogotá", "Apartamento", 4
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(solicitud)
        );

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarCapacidadCeroAlCrear() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion, "Calle 123", "Bogotá", "Apartamento", 0
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(solicitud)
        );

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarCapacidadNegativaAlCrear() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion, "Calle 123", "Bogotá", "Apartamento", -2
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(solicitud)
        );

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeActualizarAlojamientoCorrectamente() {
        Alojamiento alojamiento = new Alojamiento();

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        " Carrera 15 ",
                        " Medellín ",
                        " Casa ",
                        6
                );

        servicio.actualizarAlojamiento(idAlojamiento, solicitud);

        ArgumentCaptor<Alojamiento> captor =
                ArgumentCaptor.forClass(Alojamiento.class);

        verify(alojamientoRepository).save(captor.capture());

        Alojamiento actualizado = captor.getValue();

        assertEquals("Carrera 15", actualizado.getUbicacion());
        assertEquals("Medellín", actualizado.getCiudad());
        assertEquals("Casa", actualizado.getTipoAlojamiento());
        assertEquals(6, actualizado.getCapacidad());
    }

    @Test
    void debeRechazarIdentificadorNuloAlActualizar() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(null, solicitud)
        );

        verify(alojamientoRepository, never()).findById(any());
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarSolicitudNulaAlActualizar() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(idAlojamiento, null)
        );

        verify(alojamientoRepository, never()).findById(any());
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarUbicacionVaciaAlActualizar() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        " ", "Bogotá", "Apartamento", 4
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        verify(alojamientoRepository, never()).findById(any());
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarCiudadVaciaAlActualizar() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", " ", "Apartamento", 4
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        verify(alojamientoRepository, never()).findById(any());
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarTipoAlojamientoVacioAlActualizar() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", " ", 4
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        verify(alojamientoRepository, never()).findById(any());
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeRechazarCapacidadNegativaAlActualizar() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", -1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        verify(alojamientoRepository, never()).findById(any());
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcionCuandoNoExisteElAlojamiento() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> servicio.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        assertEquals(
                "No se encontró el alojamiento asociado a la publicación.",
                exception.getMessage()
        );

        verify(alojamientoRepository, never()).save(any());
    }
}