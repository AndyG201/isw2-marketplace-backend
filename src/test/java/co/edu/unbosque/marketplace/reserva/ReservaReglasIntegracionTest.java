package co.edu.unbosque.marketplace.reserva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.cancelacion.application.CancelacionReservaService;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.ExcepcionDominio;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.application.ReservaAnfitrionService;
import co.edu.unbosque.marketplace.reserva.application.ReservaConfirmacionService;
import co.edu.unbosque.marketplace.reserva.application.ReservaDecisionService;
import co.edu.unbosque.marketplace.reserva.application.ReservaHistorialService;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoAprobadoService;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoIntentoService;
import co.edu.unbosque.marketplace.reserva.application.ReservaRetencionScheduler;
import co.edu.unbosque.marketplace.reserva.application.ReservaRetencionService;
import co.edu.unbosque.marketplace.reserva.application.ReservaSolicitudService;
import co.edu.unbosque.marketplace.reserva.application.dto.CrearReservaRequest;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaSolicitadaEvent;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaReglasIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private ReservaSolicitudService solicitudes;

    @Autowired
    private ReservaConfirmacionService confirmaciones;

    @Autowired
    private ReservaPagoIntentoService intentosPago;

    @Autowired
    private ReservaPagoAprobadoService pagosAprobados;

    @Autowired
    private ReservaDecisionService decisiones;

    @Autowired
    private ReservaRetencionService retenciones;

    @Autowired
    private ReservaRetencionScheduler expiracionRetenciones;

    @Autowired
    private ReservaHistorialService historial;

    @Autowired
    private ReservaAnfitrionService reservasAnfitrion;

    @Autowired
    private CancelacionReservaService cancelaciones;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private final List<EscenarioReservaPrueba> escenarios = new ArrayList<>();

    @BeforeEach
    void pasarelaAprueba() {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
    }

    @AfterEach
    void limpiar() {
        for (int i = escenarios.size() - 1; i >= 0; i--) {
            escenarios.get(i).limpiar();
        }
    }

    @Test
    void solicitarExigeSesionCotizacionYClave() {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        CrearReservaRequest solicitud = new CrearReservaRequest(escenario.nuevaCotizacion(30, 2));

        assertError(SesionRequeridaException.class, "Debes iniciar sesión para solicitar una reserva.",
                () -> solicitudes.solicitar(null, solicitud, "clave"));
        assertError(DatosInvalidosException.class, "Debes indicar la cotización para solicitar una reserva.",
                () -> solicitudes.solicitar(escenario.idHuesped, new CrearReservaRequest(null), "clave"));
        assertError(DatosInvalidosException.class,
                "Debes enviar una clave de idempotencia de máximo 100 caracteres.",
                () -> solicitudes.solicitar(escenario.idHuesped, solicitud, " "));
    }

    @Test
    void solicitarRechazaFechasPasadasCapacidadYPublicacionInactiva() throws Exception {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");

        assertRespuesta(escenario.solicitar(escenario.idHuesped, escenario.nuevaCotizacion(-1, 2), "pasada"),
                400, "Las fechas de la cotización no son válidas.");

        UUID muchosHuespedes = escenario.nuevaCotizacion(30, 2);
        jdbc.update("UPDATE cotizacion SET cantidad_huespedes = 3 WHERE id_cotizacion = ?", muchosHuespedes);
        assertRespuesta(escenario.solicitar(escenario.idHuesped, muchosHuespedes, "capacidad"),
                400, "La cantidad de huéspedes supera la capacidad del alojamiento.");

        jdbc.update("UPDATE publicacion SET estado = 'PAUSADA' WHERE id_publicacion = ?", escenario.idPublicacion);
        assertRespuesta(escenario.solicitar(escenario.idHuesped, escenario.nuevaCotizacion(30, 2), "pausada"),
                409, "La publicación no está disponible para nuevas reservas.");
    }

    @Test
    void solicitarRechazaNochesOcupadasYUnaCotizacionYaSolicitada() throws Exception {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID ocupada = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, ocupada);

        assertRespuesta(escenario.solicitar(escenario.idOtroHuesped, escenario.nuevaCotizacion(30, 2), "ocupada"),
                409, "El alojamiento no está disponible para las fechas indicadas.");

        UUID cotizacion = escenario.nuevaCotizacion(50, 2);
        escenario.solicitarReserva(escenario.idHuesped, cotizacion);
        assertRespuesta(escenario.solicitar(escenario.idHuesped, cotizacion, "otra-clave-" + cotizacion),
                409, "Esta cotización ya tiene una solicitud de reserva.");
    }

    @Test
    void retenerYConfirmarValidanSesionModoYEstado() throws Exception {
        EscenarioReservaPrueba inmediata = escenario("INMEDIATA");
        EscenarioReservaPrueba aprobacion = escenario("APROBACION");
        UUID confirmada = inmediata.reservaConfirmada(30, 2);
        UUID solicitudAprobacion = aprobacion.solicitarReserva(aprobacion.idHuesped, aprobacion.nuevaCotizacion(30, 2));

        assertError(SesionRequeridaException.class, "Debes iniciar sesión.",
                () -> confirmaciones.iniciarRetencion(null, confirmada));
        assertError(RecursoNoEncontradoException.class, "La reserva no existe.",
                () -> confirmaciones.iniciarRetencion(inmediata.idHuesped, UUID.randomUUID()));
        assertError(ConflictoNegocioException.class, "La reserva no está pendiente de pago.",
                () -> confirmaciones.iniciarRetencion(inmediata.idHuesped, confirmada));
        assertError(ConflictoNegocioException.class,
                "La confirmación inmediata solo aplica a reservas de aceptación inmediata.",
                () -> confirmaciones.iniciarRetencion(aprobacion.idHuesped, solicitudAprobacion));
    }

    @Test
    void confirmarValidaLaClaveYLaRetencion() throws Exception {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID pagada = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, pagada);
        String clave = "pago-" + pagada;
        escenario.pagar(escenario.idHuesped, pagada, clave);
        UUID vencida = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(40, 2));
        escenario.retener(escenario.idHuesped, vencida);
        escenario.vencerRetencion(vencida);

        assertError(DatosInvalidosException.class, "Debes enviar Idempotency-Key de máximo 100 caracteres.",
                () -> confirmaciones.confirmar(escenario.idHuesped, vencida, " "));
        assertError(ConflictoNegocioException.class, "La clave de idempotencia ya corresponde a otra operación.",
                () -> confirmaciones.confirmar(escenario.idHuesped, vencida, clave));
        assertError(PlazoVencidoException.class, "La retención expiró; solicita una nueva reserva.",
                () -> confirmaciones.confirmar(escenario.idHuesped, vencida, "nueva-" + vencida));
    }

    @Test
    void elIntentoDePagoValidaSesionReservaYClave() throws Exception {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID pagada = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, pagada);
        String clave = "pago-" + pagada;
        escenario.pagar(escenario.idHuesped, pagada, clave);
        UUID otra = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(40, 2));

        assertError(SesionRequeridaException.class, "Debes iniciar sesión.",
                () -> intentosPago.preparar(null, otra, "clave"));
        assertError(DatosInvalidosException.class, "El identificador de la reserva es obligatorio.",
                () -> intentosPago.preparar(escenario.idHuesped, null, "clave"));
        assertError(DatosInvalidosException.class, "Debes enviar Idempotency-Key de máximo 100 caracteres.",
                () -> intentosPago.preparar(escenario.idHuesped, otra, " "));
        assertError(ConflictoNegocioException.class, "La clave de idempotencia ya corresponde a otra reserva.",
                () -> intentosPago.preparar(escenario.idHuesped, otra, clave));
    }

    @Test
    void elPagoDeUnaSolicitudAprobadaValidaSesionClaveYPlazo() throws Exception {
        EscenarioReservaPrueba inmediata = escenario("INMEDIATA");
        EscenarioReservaPrueba aprobacion = escenario("APROBACION");
        UUID confirmada = inmediata.reservaConfirmada(30, 2);
        String claveUsada = jdbc.queryForObject(
                "SELECT clave_idempotencia FROM pago WHERE id_reserva = ?", String.class, confirmada);
        UUID aprobada = aprobacion.solicitarReserva(aprobacion.idHuesped, aprobacion.nuevaCotizacion(30, 2));
        decidir(aprobacion, aprobada, "aprobar");
        UUID inmediataPendiente = inmediata.solicitarReserva(inmediata.idHuesped, inmediata.nuevaCotizacion(60, 2));

        assertError(SesionRequeridaException.class, "Debes iniciar sesión.",
                () -> pagosAprobados.pagar(null, aprobada, "clave"));
        assertError(DatosInvalidosException.class, "Debes enviar Idempotency-Key de máximo 100 caracteres.",
                () -> pagosAprobados.pagar(aprobacion.idHuesped, aprobada, " "));
        assertError(RecursoNoEncontradoException.class, "La reserva no existe.",
                () -> pagosAprobados.pagar(aprobacion.idHuesped, UUID.randomUUID(), "clave"));
        assertError(AccesoNoPermitidoException.class, "La reserva pertenece a otro usuario.",
                () -> pagosAprobados.pagar(aprobacion.idOtroHuesped, aprobada, "clave"));
        assertError(ConflictoNegocioException.class, "La clave de idempotencia ya corresponde a otra operación.",
                () -> pagosAprobados.pagar(inmediata.idHuesped, inmediataPendiente, claveUsada));
        assertError(ConflictoNegocioException.class, "La reserva no está aprobada y pendiente de pago.",
                () -> pagosAprobados.pagar(inmediata.idHuesped, inmediataPendiente, "clave-" + inmediataPendiente));

        aprobacion.vencerRetencion(aprobada);
        assertError(PlazoVencidoException.class, "El plazo de pago venció. Solicita una nueva reserva.",
                () -> pagosAprobados.pagar(aprobacion.idHuesped, aprobada, "clave-" + aprobada));
    }

    @Test
    void decidirValidaSesionAnfitrionYPlazo() throws Exception {
        EscenarioReservaPrueba escenario = escenario("APROBACION");
        UUID solicitud = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));

        assertError(SesionRequeridaException.class, "Debes iniciar sesión como anfitrión.",
                () -> decisiones.aprobar(null, solicitud));
        assertError(AccesoNoPermitidoException.class, "La solicitud no corresponde a un alojamiento tuyo.",
                () -> decisiones.aprobar(escenario.idHuesped, solicitud));

        jdbc.update("UPDATE reserva SET retencion_hasta = NULL WHERE id_reserva = ?", solicitud);
        assertError(ConflictoNegocioException.class, "La solicitud no tiene un plazo de retención válido.",
                () -> decisiones.aprobar(escenario.idAnfitrion, solicitud));
    }

    @Test
    void retenerUnaSolicitudValidaReservaPublicacionYNoches() throws Exception {
        EscenarioReservaPrueba escenario = escenario("APROBACION");
        UUID ocupando = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        UUID sinNoches = reservaSinRetener(escenario, escenario.nuevaCotizacion(30, 2));

        assertError(RecursoNoEncontradoException.class, "La reserva solicitada ya no existe.",
                () -> retenciones.retenerSolicitud(evento(escenario, UUID.randomUUID())));
        assertError(ConflictoNegocioException.class,
                "El periodo ya fue ocupado por otra reserva o no está disponible.",
                () -> retenciones.retenerSolicitud(evento(escenario, sinNoches)));

        jdbc.update("UPDATE reserva SET retencion_hasta = NULL WHERE id_reserva = ?", ocupando);
        jdbc.update("UPDATE publicacion SET estado = 'PAUSADA' WHERE id_publicacion = ?", escenario.idPublicacion);
        assertError(ConflictoNegocioException.class,
                "La publicación dejó de estar disponible para recibir reservas.",
                () -> retenciones.retenerSolicitud(evento(escenario, ocupando)));
    }

    @Test
    void unEventoRepetidoDespuesDelVencimientoNoDebeVolverARetenerLaSolicitud() throws Exception {
        EscenarioReservaPrueba escenario = escenario("APROBACION");
        UUID solicitud = escenario.solicitarReserva(
                escenario.idHuesped,
                escenario.nuevaCotizacion(30, 2));

        assertEquals(2, escenario.nochesOcupadas());
        escenario.vencerRetencion(solicitud);

        retenciones.retenerSolicitud(evento(escenario, solicitud));

        assertEquals("RECHAZADA", escenario.estadoReserva(solicitud));
        assertEquals(0, escenario.nochesOcupadas());
    }

    @Test
    void consultarReservasValidaSesionYPropietario() throws Exception {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID reserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));

        assertError(SesionRequeridaException.class, "Debes iniciar sesión como huésped.",
                () -> historial.consultarMiReserva(null, reserva));
        MvcResult ajena = mockMvc.perform(get("/reservas/mis-reservas/{id}", reserva)
                        .header("Authorization", escenario.token(escenario.idOtroHuesped)))
                .andReturn();
        assertRespuesta(ajena, 403, "No tienes permiso para consultar esta reserva.");

        assertError(SesionRequeridaException.class, "Debes iniciar sesión como anfitrión.",
                () -> reservasAnfitrion.consultarReserva(null, reserva));
        assertError(DatosInvalidosException.class, "El identificador de la reserva es obligatorio.",
                () -> reservasAnfitrion.consultarReserva(escenario.idAnfitrion, null));
        assertError(RecursoNoEncontradoException.class, "La reserva no existe.",
                () -> reservasAnfitrion.consultarReserva(escenario.idAnfitrion, UUID.randomUUID()));
    }

    @Test
    void cancelarValidaSesionMotivoYEstado() throws Exception {
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID reserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, reserva);

        assertError(SesionRequeridaException.class, "Debes iniciar sesión como huésped.",
                () -> cancelaciones.cancelar(null, reserva, "Cambio de planes"));
        assertError(DatosInvalidosException.class, "Indica un motivo de cancelación de máximo 1000 caracteres.",
                () -> cancelaciones.cancelar(escenario.idHuesped, reserva, " "));

        escenario.vencerRetencion(reserva);
        expiracionRetenciones.expirarReservasSinPago();
        MvcResult cancelacion = mockMvc.perform(post("/reservas/{id}/cancelacion", reserva)
                        .header("Authorization", escenario.token(escenario.idHuesped))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cambio de planes\"}"))
                .andReturn();
        assertRespuesta(cancelacion, 409, "La reserva no se puede cancelar en su estado actual: EXPIRADA.");
    }

    private EscenarioReservaPrueba escenario(String modoAceptacion) {
        EscenarioReservaPrueba escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, modoAceptacion, "FLEXIBLE", 100, 24);
        escenarios.add(escenario);
        return escenario;
    }

    private UUID reservaSinRetener(EscenarioReservaPrueba escenario, UUID idCotizacion) {
        UUID idReserva = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO reserva (id_reserva, id_huesped, id_cotizacion, id_politica_aplicada, modo_aceptacion,
                    estado, clave_idempotencia, requiere_revision_manual, fecha_solicitud)
                VALUES (?, ?, ?, ?, 'APROBACION', 'PENDIENTE_APROBACION', ?, true, now())
                """, idReserva, escenario.idOtroHuesped, idCotizacion, escenario.idPolitica, "sin-noches-" + idReserva);
        return idReserva;
    }

    private static ReservaSolicitadaEvent evento(EscenarioReservaPrueba escenario, UUID idReserva) {
        return new ReservaSolicitadaEvent(escenario.idAnfitrion, idReserva, escenario.idPublicacion,
                "APROBACION", OffsetDateTime.now());
    }

    private void decidir(EscenarioReservaPrueba escenario, UUID idReserva, String decision) throws Exception {
        mockMvc.perform(patch("/reservas/{id}/" + decision, idReserva)
                        .header("Authorization", escenario.token(escenario.idAnfitrion)))
                .andReturn();
    }

    private static void assertRespuesta(MvcResult resultado, int estado, String mensaje) throws Exception {
        assertEquals(estado, resultado.getResponse().getStatus());
        assertEquals(mensaje, JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.mensaje"));
    }

    private static void assertError(Class<? extends ExcepcionDominio> tipo, String mensaje, Executable accion) {
        ExcepcionDominio error = assertThrows(tipo, accion);
        assertEquals(mensaje, error.getMessage());
    }
}
