package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaQuery;
import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.SolicitudCambioResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.SolicitudCambioPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.SolicitudCambioPublicacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaQuery;

@ExtendWith(MockitoExtension.class)
class CambiosPublicacionServiceTest {

    private static final UUID PUBLICACION = UUID.randomUUID();
    private static final UUID ANFITRION = UUID.randomUUID();
    private static final LocalDate HOY = LocalDate.of(2026, 11, 20);

    @Mock
    private SolicitudCambioPublicacionRepository solicitudes;

    @Mock
    private CotizacionReservaQuery cotizaciones;

    @Mock
    private ReservaAfectadaQuery reservas;

    private CambiosPublicacionService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CambiosPublicacionService(solicitudes, cotizaciones, reservas);
    }

    private Publicacion publicacion() {
        Publicacion publicacion = new Publicacion();
        publicacion.setTitulo("Loft en Chapinero");
        publicacion.setDescripcion("Con vista a los cerros");
        publicacion.setReglas("No fumar");
        return publicacion;
    }

    private AlojamientoPublicacionInfo alojamiento() {
        return new AlojamientoPublicacionInfo(
                UUID.randomUUID(), ANFITRION, "Calle 60", "Bogotá", "Apartamento", 4, 2, 3, 1);
    }

    private EditarPublicacionRequest sinCambios() {
        EditarPublicacionRequest solicitud = new EditarPublicacionRequest();
        solicitud.setTitulo(" Loft en Chapinero ");
        solicitud.setDescripcion("Con vista a los cerros");
        solicitud.setReglas("No fumar");
        solicitud.setUbicacion("Calle 60");
        solicitud.setCiudad("Bogotá");
        solicitud.setTipoAlojamiento("Apartamento");
        solicitud.setCapacidad(4);
        solicitud.setHabitaciones(2);
        solicitud.setCamas(3);
        solicitud.setBanos(1);
        return solicitud;
    }

    @Test
    void soloLasPublicacionesYaAprobadasPasanARevisionAlEditar() {
        assertTrue(servicio.requiereRevision("ACTIVA"));
        assertTrue(servicio.requiereRevision("PAUSADA"));
        assertFalse(servicio.requiereRevision("BORRADOR"));
        assertFalse(servicio.requiereRevision(null));
    }

    @Test
    void sinCambiosEnLoRevisadoNoReportaCampos() {
        assertEquals(List.of(), servicio.camposSensiblesModificados(publicacion(), alojamiento(), sinCambios()));
    }

    @Test
    void reportaCadaCampoRevisadoQueCambio() {
        EditarPublicacionRequest solicitud = sinCambios();
        solicitud.setTitulo("Loft renovado");
        solicitud.setDescripcion("Nueva descripción");
        solicitud.setReglas("Sin mascotas");
        solicitud.setImagenes(List.of(mock(MultipartFile.class)));
        solicitud.setUbicacion("Calle 61");
        solicitud.setCiudad("Medellín");
        solicitud.setTipoAlojamiento("Casa");
        solicitud.setCapacidad(5);
        solicitud.setHabitaciones(3);
        solicitud.setCamas(4);
        solicitud.setBanos(2);

        assertEquals(
                List.of("Título", "Descripción", "Reglas", "Fotos", "Ubicación", "Ciudad",
                        "Tipo de alojamiento", "Capacidad", "Habitaciones", "Camas", "Baños"),
                servicio.camposSensiblesModificados(publicacion(), alojamiento(), solicitud));
    }

    @Test
    void cambiarElPropietarioDelInmueblePasaARevision() {
        AlojamientoPublicacionInfo conPropietario = new AlojamientoPublicacionInfo(
                UUID.randomUUID(), ANFITRION, "Calle 60", "Bogotá", "Apartamento", 4, 2, 3, 1, "Ana Gómez");

        EditarPublicacionRequest igual = sinCambios();
        igual.setPropietarioInmueble(" Ana Gómez ");
        EditarPublicacionRequest otro = sinCambios();
        otro.setPropietarioInmueble("Inversiones Rojas");

        assertEquals(List.of(), servicio.camposSensiblesModificados(publicacion(), conPropietario, sinCambios()));
        assertEquals(List.of(), servicio.camposSensiblesModificados(publicacion(), conPropietario, igual));
        assertEquals(List.of("Propietario del inmueble"),
                servicio.camposSensiblesModificados(publicacion(), conPropietario, otro));
    }

    @Test
    void exigeUnMensajeParaElAdministrador() {
        assertEquals("Cambié las fotos", servicio.validarMensaje("  Cambié las fotos "));
        assertThrows(IllegalArgumentException.class, () -> servicio.validarMensaje(" "));
        assertThrows(IllegalArgumentException.class, () -> servicio.validarMensaje(null));
        assertThrows(IllegalArgumentException.class, () -> servicio.validarMensaje("x".repeat(1001)));
    }

    @Test
    void noDejaBajarLaCapacidadPorDebajoDeUnaReservaFuturaVigente() {
        UUID futura = UUID.randomUUID();
        UUID pasada = UUID.randomUUID();
        UUID cancelada = UUID.randomUUID();
        when(cotizaciones.consultarPorPublicacion(PUBLICACION)).thenReturn(List.of(
                new CotizacionReservaInfo(futura, PUBLICACION, HOY.plusDays(5), HOY.plusDays(8), 3),
                new CotizacionReservaInfo(pasada, PUBLICACION, HOY.minusDays(8), HOY.minusDays(5), 6),
                new CotizacionReservaInfo(cancelada, PUBLICACION, HOY.plusDays(9), HOY.plusDays(10), 6)));
        when(reservas.consultarPorCotizaciones(any())).thenReturn(List.of(
                new ReservaAfectadaInfo(UUID.randomUUID(), UUID.randomUUID(), futura, "CONFIRMADA"),
                new ReservaAfectadaInfo(UUID.randomUUID(), UUID.randomUUID(), cancelada, "CANCELADA")));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.validarCapacidad(PUBLICACION, 2, HOY));

        assertTrue(error.getMessage().contains("3 huéspedes"));
        assertDoesNotThrow(() -> servicio.validarCapacidad(PUBLICACION, 3, HOY));
    }

    @Test
    void sinReservasFuturasPermiteCualquierCapacidad() {
        when(cotizaciones.consultarPorPublicacion(PUBLICACION)).thenReturn(List.of());

        assertDoesNotThrow(() -> servicio.validarCapacidad(PUBLICACION, 1, HOY));
        verify(reservas, never()).consultarPorCotizaciones(any());
    }

    @Test
    void registraLaSolicitudYCierraLaAnteriorPendiente() {
        SolicitudCambioPublicacion anterior = new SolicitudCambioPublicacion(
                UUID.randomUUID(), PUBLICACION, ANFITRION, "Antes", "Título", "ACTIVA", OffsetDateTime.now());
        when(solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(PUBLICACION))
                .thenReturn(Optional.of(anterior));
        OffsetDateTime ahora = OffsetDateTime.now();

        servicio.registrarSolicitud(PUBLICACION, ANFITRION, "Nuevo mensaje",
                List.of("Fotos", "Ciudad"), "PAUSADA", ahora);

        assertEquals(ahora, anterior.getFechaResolucion());
        ArgumentCaptor<SolicitudCambioPublicacion> guardada =
                ArgumentCaptor.forClass(SolicitudCambioPublicacion.class);
        verify(solicitudes).save(guardada.capture());
        assertEquals("Nuevo mensaje", guardada.getValue().getMensaje());
        assertEquals("Fotos\nCiudad", guardada.getValue().getCampos());
        assertEquals("PAUSADA", guardada.getValue().getEstadoAnterior());
        assertEquals(ANFITRION, guardada.getValue().getIdAnfitrion());
    }

    @Test
    void consultaLaSolicitudPendienteParaElAdministrador() {
        OffsetDateTime fecha = OffsetDateTime.now();
        when(solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(PUBLICACION))
                .thenReturn(Optional.of(new SolicitudCambioPublicacion(
                        UUID.randomUUID(), PUBLICACION, ANFITRION, "Cambié fotos", "Fotos\nCamas", "ACTIVA", fecha)));

        SolicitudCambioResponse respuesta = servicio.pendiente(PUBLICACION).orElseThrow();

        assertEquals(List.of("Fotos", "Camas"), respuesta.campos());
        assertEquals("Cambié fotos", respuesta.mensaje());
        assertEquals("ACTIVA", respuesta.estadoAnterior());
        assertEquals(fecha, respuesta.fechaSolicitud());
    }

    @Test
    void alResolverDevuelveElEstadoQueTeniaAntesDeLaRevision() {
        SolicitudCambioPublicacion pendiente = new SolicitudCambioPublicacion(
                UUID.randomUUID(), PUBLICACION, ANFITRION, "m", "Fotos", "PAUSADA", OffsetDateTime.now());
        when(solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(PUBLICACION))
                .thenReturn(Optional.of(pendiente));
        OffsetDateTime ahora = OffsetDateTime.now();

        assertEquals(Optional.of("PAUSADA"), servicio.resolver(PUBLICACION, ahora));
        assertEquals(ahora, pendiente.getFechaResolucion());
        verify(solicitudes).save(pendiente);
    }

    @Test
    void sinSolicitudPendienteNoHayNadaQueResolver() {
        when(solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(PUBLICACION))
                .thenReturn(Optional.empty());

        assertEquals(Optional.empty(), servicio.resolver(PUBLICACION, OffsetDateTime.now()));
        assertEquals(Optional.empty(), servicio.pendiente(PUBLICACION));
    }
}
