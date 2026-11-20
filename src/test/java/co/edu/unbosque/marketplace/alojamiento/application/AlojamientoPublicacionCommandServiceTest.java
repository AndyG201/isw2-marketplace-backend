package co.edu.unbosque.marketplace.alojamiento.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

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

    private static final UUID ANFITRION = UUID.randomUUID();

    @Mock
    private AlojamientoRepository repositorio;

    @InjectMocks
    private AlojamientoPublicacionCommandService servicio;

    private Alojamiento guardado() {
        ArgumentCaptor<Alojamiento> captor = ArgumentCaptor.forClass(Alojamiento.class);
        verify(repositorio).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void creaElAlojamientoConSuDistribucion() {
        UUID id = servicio.crearAlojamiento(new CrearAlojamientoParaPublicacion(
                ANFITRION, " Calle 1 ", " Bogotá ", " Casa ", 6, 3, 4, 2));

        Alojamiento alojamiento = guardado();
        assertAll(
                () -> assertEquals(id, alojamiento.getIdAlojamiento()),
                () -> assertEquals("Bogotá", alojamiento.getCiudad()),
                () -> assertEquals(3, alojamiento.getHabitaciones()),
                () -> assertEquals(4, alojamiento.getCamas()),
                () -> assertEquals(2, alojamiento.getBanos()));
    }

    @Test
    void sinDistribucionUsaUnoPorDefecto() {
        servicio.crearAlojamiento(new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Bogotá", "Casa", 2));

        Alojamiento alojamiento = guardado();
        assertAll(
                () -> assertEquals(1, alojamiento.getHabitaciones()),
                () -> assertEquals(1, alojamiento.getCamas()),
                () -> assertEquals(1, alojamiento.getBanos()));
    }

    @Test
    void rechazaDistribucionesFueraDeLosLimites() {
        CrearAlojamientoParaPublicacion sinHabitaciones = new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Bogotá", "Casa", 2, 0, 1, 1);
        CrearAlojamientoParaPublicacion muchasCamas = new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Bogotá", "Casa", 2, 1, 51, 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(sinHabitaciones));
        assertEquals("El número de habitaciones debe estar entre 1 y 50.", error.getMessage());
        assertThrows(IllegalArgumentException.class, () -> servicio.crearAlojamiento(muchasCamas));
        verify(repositorio, never()).save(any());
    }

    @Test
    void actualizaLaDistribucionIndicadaYConservaLaQueNoSeEnvia() {
        UUID id = UUID.randomUUID();
        Alojamiento existente = new Alojamiento();
        existente.setIdAlojamiento(id);
        existente.setHabitaciones(2);
        existente.setCamas(3);
        existente.setBanos(1);
        when(repositorio.findById(id)).thenReturn(Optional.of(existente));

        servicio.actualizarAlojamiento(id, new ActualizarAlojamientoParaPublicacion(
                "Calle 2", "Cali", "Finca", 8, 4, null, 3));

        Alojamiento alojamiento = guardado();
        assertAll(
                () -> assertEquals("Cali", alojamiento.getCiudad()),
                () -> assertEquals(4, alojamiento.getHabitaciones()),
                () -> assertEquals(3, alojamiento.getCamas()),
                () -> assertEquals(3, alojamiento.getBanos()));
    }

    @Test
    void noActualizaConUnaDistribucionInvalida() {
        UUID id = UUID.randomUUID();
        ActualizarAlojamientoParaPublicacion solicitud = new ActualizarAlojamientoParaPublicacion(
                "Calle 2", "Cali", "Finca", 8, null, null, 0);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarAlojamiento(id, solicitud));
        assertEquals("El número de baños debe estar entre 1 y 50.", error.getMessage());
        verify(repositorio, never()).save(any());
    }

    @Test
    void guardaElPropietarioDelInmuebleSinEspaciosSobrantes() {
        servicio.crearAlojamiento(new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Bogotá", "Casa", 2, 1, 1, 1, "  Ana Gómez  "));
        assertEquals("Ana Gómez", guardado().getPropietarioInmueble());
    }

    @Test
    void sinPropietarioLoDejaVacio() {
        servicio.crearAlojamiento(new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Bogotá", "Casa", 2, 1, 1, 1, "   "));
        assertNull(guardado().getPropietarioInmueble());
    }

    @Test
    void rechazaUnPropietarioDemasiadoLargo() {
        CrearAlojamientoParaPublicacion solicitud = new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Bogotá", "Casa", 2, 1, 1, 1, "x".repeat(121));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.crearAlojamiento(solicitud));
        assertEquals("El nombre del propietario del inmueble no puede superar 120 caracteres.",
                error.getMessage());
        verify(repositorio, never()).save(any());
    }

    @Test
    void alEditarCambiaElPropietarioSoloSiSeEnvia() {
        UUID id = UUID.randomUUID();
        Alojamiento existente = new Alojamiento();
        existente.setIdAlojamiento(id);
        existente.setPropietarioInmueble("Ana Gómez");
        when(repositorio.findById(id)).thenReturn(Optional.of(existente));

        servicio.actualizarAlojamiento(id, new ActualizarAlojamientoParaPublicacion(
                "Calle 2", "Cali", "Finca", 8, null, null, null));
        assertEquals("Ana Gómez", existente.getPropietarioInmueble());

        servicio.actualizarAlojamiento(id, new ActualizarAlojamientoParaPublicacion(
                "Calle 2", "Cali", "Finca", 8, null, null, null, " Inversiones Rojas "));
        assertEquals("Inversiones Rojas", existente.getPropietarioInmueble());
    }

    @Test
    void guardaElPaisDelAlojamientoConSuCodigo() {
        servicio.crearAlojamiento(new CrearAlojamientoParaPublicacion(
                ANFITRION, "Calle 1", "Lima", "Casa", 2, 1, 1, 1, null, " Peru ", "pe"));

        Alojamiento alojamiento = guardado();
        assertEquals("Peru", alojamiento.getPais());
        assertEquals("PE", alojamiento.getCodigoPais());
    }

    @Test
    void ignoraUnCodigoDePaisInvalidoYConservaElPaisSiNoSeEnvia() {
        UUID id = UUID.randomUUID();
        Alojamiento existente = new Alojamiento();
        existente.setIdAlojamiento(id);
        existente.setPais("Colombia");
        existente.setCodigoPais("CO");
        when(repositorio.findById(id)).thenReturn(Optional.of(existente));

        servicio.actualizarAlojamiento(id, new ActualizarAlojamientoParaPublicacion(
                "Calle 2", "Cali", "Finca", 8, null, null, null));
        assertEquals("CO", existente.getCodigoPais());

        servicio.actualizarAlojamiento(id, new ActualizarAlojamientoParaPublicacion(
                "Calle 2", "Quito", "Finca", 8, null, null, null, null, "Ecuador", "ECU"));
        assertEquals("Ecuador", existente.getPais());
        assertNull(existente.getCodigoPais());
    }
}
