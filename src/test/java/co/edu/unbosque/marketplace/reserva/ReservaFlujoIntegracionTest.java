package co.edu.unbosque.marketplace.reserva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.pago.application.ReembolsoTrabajo;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsosPendientes;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.application.ReservaAprobacionRetencionScheduler;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoConciliacionWorker;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoIntentoService;
import co.edu.unbosque.marketplace.reserva.application.ReservaRetencionScheduler;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaFlujoIntegracionTest {

    private static final BigDecimal MONTO_DOS_NOCHES = new BigDecimal("220000.00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private ReservaPagoConciliacionWorker conciliacion;

    @Autowired
    private ReservaRetencionScheduler expiracionRetenciones;

    @Autowired
    private ReservaAprobacionRetencionScheduler expiracionSolicitudes;

    @Autowired
    private ReservaPagoIntentoService intentosPago;

    @Autowired
    private RegistroPagos registroPagos;

    @Autowired
    private ReembolsosPendientes reembolsosPendientes;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;

    @AfterEach
    void limpiar() {
        if (escenario != null) {
            escenario.limpiar();
        }
    }

    @Test
    void unaReservaInmediataQuedaConfirmadaTrasSolicitarRetenerYPagar() throws Exception {
        escenario = escenario("INMEDIATA");
        pasarelaAprueba();
        UUID idCotizacion = escenario.nuevaCotizacion(30, 2);

        MvcResult solicitud = escenario.solicitar(escenario.idHuesped, idCotizacion, "solicitud-flujo");
        assertEquals(201, solicitud.getResponse().getStatus());
        assertEquals("PENDIENTE_PAGO", campo(solicitud, "$.estado"));
        UUID idReserva = UUID.fromString(campo(solicitud, "$.idReserva"));

        MvcResult retencion = escenario.retener(escenario.idHuesped, idReserva);
        assertEquals(200, retencion.getResponse().getStatus());
        assertNotNull(campo(retencion, "$.retencionHasta"));
        assertEquals(2, escenario.nochesOcupadas());

        MvcResult pago = escenario.pagar(escenario.idHuesped, idReserva, "pago-flujo-" + idReserva);
        assertEquals(200, pago.getResponse().getStatus());
        assertEquals("CONFIRMADA", campo(pago, "$.estadoReserva"));
        assertEquals("PAGADO", campo(pago, "$.estadoPago"));
        assertEquals("pi_prueba", campo(pago, "$.referenciaPago"));
        assertEquals(0, MONTO_DOS_NOCHES.compareTo(new BigDecimal(campo(pago, "$.monto").toString())));

        assertEquals("CONFIRMADA", escenario.estadoReserva(idReserva));
        assertEquals(2, escenario.nochesOcupadas());
        verify(pasarela, times(1)).procesar(any(), eq("pago-flujo-" + idReserva));
    }

    @Test
    void repetirLaSolicitudConLaMismaClaveDevuelveLaMismaReserva() throws Exception {
        escenario = escenario("INMEDIATA");
        UUID idCotizacion = escenario.nuevaCotizacion(30, 2);
        String clave = "solicitud-repetida-" + idCotizacion;

        MvcResult primera = escenario.solicitar(escenario.idHuesped, idCotizacion, clave);
        MvcResult segunda = escenario.solicitar(escenario.idHuesped, idCotizacion, clave);

        assertEquals(201, primera.getResponse().getStatus());
        assertEquals(201, segunda.getResponse().getStatus());
        assertEquals((String) campo(primera, "$.idReserva"), campo(segunda, "$.idReserva"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM reserva WHERE id_cotizacion = ?", Integer.class, idCotizacion));

        MvcResult otraCotizacion = escenario.solicitar(
                escenario.idHuesped, escenario.nuevaCotizacion(40, 2), clave);
        assertEquals(409, otraCotizacion.getResponse().getStatus());
    }

    @Test
    void repetirElPagoConLaMismaClaveNoCobraDosVeces() throws Exception {
        escenario = escenario("INMEDIATA");
        pasarelaAprueba();
        UUID idReserva = reservaRetenida();
        String clave = "pago-repetido-" + idReserva;

        MvcResult primero = escenario.pagar(escenario.idHuesped, idReserva, clave);
        MvcResult segundo = escenario.pagar(escenario.idHuesped, idReserva, clave);

        assertEquals(200, primero.getResponse().getStatus());
        assertEquals(200, segundo.getResponse().getStatus());
        assertEquals((String) campo(primero, "$.idPago"), campo(segundo, "$.idPago"));
        assertEquals("PAGADO", campo(segundo, "$.estadoPago"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM pago WHERE id_reserva = ?", Integer.class, idReserva));
        verify(pasarela, times(1)).procesar(any(), eq(clave));
    }

    @Test
    void otroHuespedNoPuedePagarNiRetenerUnaReservaAjena() throws Exception {
        escenario = escenario("INMEDIATA");
        UUID idReserva = reservaRetenida();

        assertEquals(403, escenario.pagar(escenario.idOtroHuesped, idReserva, "pago-ajeno").getResponse().getStatus());
        assertEquals(403, escenario.retener(escenario.idOtroHuesped, idReserva).getResponse().getStatus());
        verify(pasarela, never()).procesar(any(), any());
        assertEquals("PENDIENTE_PAGO", escenario.estadoReserva(idReserva));
    }

    @Test
    void dosRetencionesSimultaneasSobreLasMismasNochesSoloDejanUnaReserva() throws Exception {
        escenario = escenario("INMEDIATA");
        UUID reservaHuesped = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        UUID reservaOtro = escenario.solicitarReserva(escenario.idOtroHuesped, escenario.nuevaCotizacion(30, 2));

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> primera = hilos.submit(retencionAlMismoTiempo(salida, escenario.idHuesped, reservaHuesped));
            Future<Integer> segunda = hilos.submit(retencionAlMismoTiempo(salida, escenario.idOtroHuesped, reservaOtro));
            salida.countDown();

            List<Integer> estados = List.of(primera.get(), segunda.get()).stream().sorted().toList();
            assertEquals(List.of(200, 409), estados);
        } finally {
            hilos.shutdownNow();
        }

        assertEquals(2, escenario.nochesOcupadas());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(DISTINCT rn.id_reserva) FROM reserva_noche rn
                WHERE rn.id_reserva IN (?, ?)
                """, Integer.class, reservaHuesped, reservaOtro));
    }

    @Test
    void siLaPasarelaFallaElPagoQuedaPendienteYLaConciliacionLoConfirma() throws Exception {
        escenario = escenario("INMEDIATA");
        when(pasarela.procesar(any(), any()))
                .thenThrow(new IllegalStateException("Stripe no respondió"))
                .thenReturn(new ResultadoPagoSandbox(true, "pi_conciliado", "Pago aprobado"));
        UUID idReserva = reservaRetenida();
        String clave = "pago-falla-" + idReserva;

        MvcResult pago = escenario.pagar(escenario.idHuesped, idReserva, clave);
        assertEquals(200, pago.getResponse().getStatus());
        assertEquals("PENDIENTE", campo(pago, "$.estadoPago"));
        assertEquals("PENDIENTE_PAGO", campo(pago, "$.estadoReserva"));
        Map<String, Object> operacion = operacion(idReserva);
        assertEquals("PENDIENTE", operacion.get("estado"));
        assertEquals(1, operacion.get("intentos"));

        MvcResult otraClave = escenario.pagar(escenario.idHuesped, idReserva, "otra-clave-" + idReserva);
        assertEquals(409, otraClave.getResponse().getStatus());

        escenario.adelantarConciliacion(idReserva);
        conciliacion.conciliarPendientes();

        assertEquals("CONFIRMADA", escenario.estadoReserva(idReserva));
        assertEquals("PAGADO", estadoPago(idReserva));
        assertEquals("COMPLETADA", operacion(idReserva).get("estado"));
        verify(pasarela, times(2)).procesar(any(), eq(clave));
    }

    @Test
    void siElProcesoSeCaeAntesDeLlamarAStripeLaConciliacionTerminaElPago() throws Exception {
        escenario = escenario("INMEDIATA");
        pasarelaAprueba();
        UUID idReserva = reservaRetenida();
        String clave = "pago-caida-" + idReserva;

        intentosPago.preparar(escenario.idHuesped, idReserva, clave);
        verify(pasarela, never()).procesar(any(), any());
        assertEquals("PENDIENTE", estadoPago(idReserva));
        assertEquals("PENDIENTE_PAGO", escenario.estadoReserva(idReserva));

        MvcResult reintento = escenario.pagar(escenario.idHuesped, idReserva, clave);
        assertEquals(200, reintento.getResponse().getStatus());
        assertEquals("CONFIRMADA", campo(reintento, "$.estadoReserva"));
        verify(pasarela, times(1)).procesar(any(), eq(clave));
    }

    @Test
    void elTrabajadorDeConciliacionRecuperaUnPagoQueQuedoAMedias() throws Exception {
        escenario = escenario("INMEDIATA");
        pasarelaAprueba();
        UUID idReserva = reservaRetenida();
        String clave = "pago-huerfano-" + idReserva;

        intentosPago.preparar(escenario.idHuesped, idReserva, clave);
        conciliacion.conciliarPendientes();

        assertEquals("CONFIRMADA", escenario.estadoReserva(idReserva));
        assertEquals("PAGADO", estadoPago(idReserva));
        assertEquals("COMPLETADA", operacion(idReserva).get("estado"));
        verify(pasarela, times(1)).procesar(any(), eq(clave));
    }

    @Test
    void trasCincoFallosLaConciliacionSeMarcaComoFallida() throws Exception {
        escenario = escenario("INMEDIATA");
        when(pasarela.procesar(any(), any())).thenThrow(new IllegalStateException("Stripe no disponible"));
        UUID idReserva = reservaRetenida();

        escenario.pagar(escenario.idHuesped, idReserva, "pago-agotado-" + idReserva);
        for (int intento = 2; intento <= 5; intento++) {
            escenario.adelantarConciliacion(idReserva);
            conciliacion.conciliarPendientes();
        }

        Map<String, Object> operacion = operacion(idReserva);
        assertEquals("FALLIDA", operacion.get("estado"));
        assertEquals(5, operacion.get("intentos"));
        assertEquals("PENDIENTE", estadoPago(idReserva));
        assertEquals("PENDIENTE_PAGO", escenario.estadoReserva(idReserva));

        escenario.adelantarConciliacion(idReserva);
        conciliacion.conciliarPendientes();
        verify(pasarela, times(5)).procesar(any(), any());
    }

    @Test
    void unaRetencionVencidaExpiraLaReservaYLiberaLasNoches() throws Exception {
        escenario = escenario("INMEDIATA");
        UUID reservaOtro = escenario.solicitarReserva(escenario.idOtroHuesped, escenario.nuevaCotizacion(30, 2));
        UUID idReserva = reservaRetenida();
        assertEquals(409, escenario.retener(escenario.idOtroHuesped, reservaOtro).getResponse().getStatus());

        escenario.vencerRetencion(idReserva);
        expiracionRetenciones.expirarReservasSinPago();

        assertEquals("EXPIRADA", escenario.estadoReserva(idReserva));
        assertEquals(0, escenario.nochesOcupadas());
        assertEquals(409, escenario.pagar(escenario.idHuesped, idReserva, "pago-expirado").getResponse().getStatus());
        assertEquals(200, escenario.retener(escenario.idOtroHuesped, reservaOtro).getResponse().getStatus());
        assertEquals(2, escenario.nochesOcupadas());
    }

    @Test
    void unaNuevaRetencionLimpiaLasNochesDeUnaReservaDePagoVencidaSinEjecutarElScheduler()
            throws Exception {
        escenario = escenario("INMEDIATA");

        UUID reservaVencida = reservaRetenida();
        escenario.vencerRetencion(reservaVencida);

        // La nueva solicitud puede crearse porque la retención anterior ya venció,
        // aunque sus filas físicas todavía no hayan sido retiradas por el scheduler.
        UUID nuevaReserva = escenario.solicitarReserva(
                escenario.idOtroHuesped,
                escenario.nuevaCotizacion(30, 2));

        assertEquals(2, nochesDeReserva(reservaVencida));

        MvcResult nuevaRetencion = escenario.retener(
                escenario.idOtroHuesped,
                nuevaReserva);

        assertEquals(200, nuevaRetencion.getResponse().getStatus());
        assertEquals(0, nochesDeReserva(reservaVencida));
        assertEquals(2, nochesDeReserva(nuevaReserva));
        assertEquals(2, escenario.nochesOcupadas());

        // El scheduler puede ejecutarse después: liberar una reserva ya limpiada
        // no debe borrar las noches que ahora pertenecen a la nueva reserva.
        expiracionRetenciones.expirarReservasSinPago();

        assertEquals("EXPIRADA", escenario.estadoReserva(reservaVencida));
        assertEquals(2, nochesDeReserva(nuevaReserva));
        assertEquals(2, escenario.nochesOcupadas());
    }

    @Test
    void unaSolicitudDeAprobacionConRetencionVigenteSigueBloqueandoLasNoches() throws Exception {
        escenario = escenario("APROBACION");

        UUID reservaVigente = escenario.solicitarReserva(
                escenario.idHuesped,
                escenario.nuevaCotizacion(30, 2));

        MvcResult nuevaSolicitud = escenario.solicitar(
                escenario.idOtroHuesped,
                escenario.nuevaCotizacion(30, 2),
                "solicitud-no-debe-robar-noches-" + reservaVigente);

        assertEquals(409, nuevaSolicitud.getResponse().getStatus());
        assertEquals("PENDIENTE_APROBACION", escenario.estadoReserva(reservaVigente));
        assertEquals(2, nochesDeReserva(reservaVigente));
        assertEquals(2, escenario.nochesOcupadas());
    }

    @Test
    void unaNuevaSolicitudLimpiaLasNochesDeUnaSolicitudAprobacionVencidaSinEjecutarElScheduler()
            throws Exception {
        escenario = escenario("APROBACION");

        UUID solicitudVencida = escenario.solicitarReserva(
                escenario.idHuesped,
                escenario.nuevaCotizacion(30, 2));
        assertEquals(2, nochesDeReserva(solicitudVencida));

        escenario.vencerRetencion(solicitudVencida);

        // La nueva solicitud dispara su listener de retención. Ese flujo debe
        // retirar las noches vencidas anteriores antes de insertar las nuevas.
        UUID nuevaSolicitud = escenario.solicitarReserva(
                escenario.idOtroHuesped,
                escenario.nuevaCotizacion(30, 2));

        assertEquals("PENDIENTE_APROBACION", escenario.estadoReserva(solicitudVencida));
        assertEquals(0, nochesDeReserva(solicitudVencida));
        assertEquals(2, nochesDeReserva(nuevaSolicitud));
        assertEquals(2, escenario.nochesOcupadas());

        // La conciliación automática posterior de la solicitud vieja debe ser
        // idempotente y no liberar las noches que ya tiene la nueva solicitud.
        expiracionSolicitudes.rechazarSolicitudesVencidas();

        assertEquals("RECHAZADA", escenario.estadoReserva(solicitudVencida));
        assertEquals(2, nochesDeReserva(nuevaSolicitud));
        assertEquals(2, escenario.nochesOcupadas());
    }

    @Test
    void elReembolsoProgramadoSeReclamaUnaSolaVezContraPostgres() throws Exception {
        escenario = escenario("INMEDIATA");
        pasarelaAprueba();
        UUID idReserva = reservaRetenida();
        String clave = "pago-reembolso-" + idReserva;
        assertEquals(200, escenario.pagar(escenario.idHuesped, idReserva, clave).getResponse().getStatus());
        UUID idPago = jdbc.queryForObject("SELECT id_pago FROM pago WHERE id_reserva = ?", UUID.class, idReserva);

        registroPagos.programarReembolso(clave, OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(1));

        List<ReembolsoTrabajo> primera = reclamadosDe(idPago);
        assertEquals(1, primera.size());
        assertEquals("pi_prueba", primera.get(0).paymentIntentId());
        assertEquals("refund-" + idPago, primera.get(0).claveIdempotencia());
        assertEquals(0, MONTO_DOS_NOCHES.compareTo(primera.get(0).monto()));
        assertEquals("EN_PROCESO", jdbc.queryForObject(
                "SELECT estado FROM reembolso_pago WHERE id_pago = ?", String.class, idPago));
        assertEquals(1, jdbc.queryForObject(
                "SELECT intentos FROM reembolso_pago WHERE id_pago = ?", Integer.class, idPago));

        // Mientras el trabajo está en proceso (reclamo vigente) otro worker no lo vuelve a tomar.
        assertEquals(0, reclamadosDe(idPago).size());
    }

    @Test
    void pagarConLaRetencionVencidaRespondeQueElPlazoTermino() throws Exception {
        escenario = escenario("INMEDIATA");
        UUID idReserva = reservaRetenida();
        escenario.vencerRetencion(idReserva);

        MvcResult pago = escenario.pagar(escenario.idHuesped, idReserva, "pago-tarde-" + idReserva);

        assertEquals(410, pago.getResponse().getStatus());
        verify(pasarela, never()).procesar(any(), any());
    }

    @Test
    void unaSolicitudAprobadaPorElAnfitrionSePuedePagarYQuedaConfirmada() throws Exception {
        escenario = escenario("APROBACION");
        pasarelaAprueba();
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));

        assertEquals("PENDIENTE_APROBACION", escenario.estadoReserva(idReserva));
        assertEquals(2, escenario.nochesOcupadas());
        assertNotNull(retencionHasta(idReserva));

        MvcResult aprobacion = decidir(escenario.idAnfitrion, idReserva, "aprobar");
        assertEquals(200, aprobacion.getResponse().getStatus());
        assertEquals("PENDIENTE_PAGO", campo(aprobacion, "$.estado"));
        assertNotNull(campo(aprobacion, "$.retencionHasta"));

        MvcResult pago = escenario.pagar(escenario.idHuesped, idReserva, "pago-aprobada-" + idReserva);
        assertEquals("CONFIRMADA", campo(pago, "$.estadoReserva"));
        assertEquals(2, escenario.nochesOcupadas());
    }

    @Test
    void unaSolicitudRechazadaLiberaLasNoches() throws Exception {
        escenario = escenario("APROBACION");
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));

        assertEquals(403, decidir(escenario.idHuesped, idReserva, "rechazar").getResponse().getStatus());

        MvcResult rechazo = decidir(escenario.idAnfitrion, idReserva, "rechazar");
        assertEquals(200, rechazo.getResponse().getStatus());
        assertEquals("RECHAZADA", campo(rechazo, "$.estado"));
        assertEquals("RECHAZADA", escenario.estadoReserva(idReserva));
        assertEquals(0, escenario.nochesOcupadas());
        assertEquals(409, decidir(escenario.idAnfitrion, idReserva, "aprobar").getResponse().getStatus());
    }

    @Test
    void unaSolicitudSinRespuestaDelAnfitrionSeRechazaAlVencerElPlazo() throws Exception {
        escenario = escenario("APROBACION");
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.vencerRetencion(idReserva);

        expiracionSolicitudes.rechazarSolicitudesVencidas();

        assertEquals("RECHAZADA", escenario.estadoReserva(idReserva));
        assertNull(retencionHasta(idReserva));
        assertEquals(0, escenario.nochesOcupadas());
    }

    private EscenarioReservaPrueba escenario(String modoAceptacion) {
        return EscenarioReservaPrueba.crear(jdbc, mockMvc, jwtUtil, usuarios, modoAceptacion, "FLEXIBLE", 100, 24);
    }

    private UUID reservaRetenida() throws Exception {
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        assertEquals(200, escenario.retener(escenario.idHuesped, idReserva).getResponse().getStatus());
        return idReserva;
    }

    private List<ReembolsoTrabajo> reclamadosDe(UUID idPago) {
        return reembolsosPendientes.reclamar(OffsetDateTime.now(ZoneOffset.UTC)).stream()
                .filter(t -> t.claveIdempotencia().equals("refund-" + idPago))
                .toList();
    }

    private int nochesDeReserva(UUID idReserva) {
        Integer total = jdbc.queryForObject(
                "SELECT count(*) FROM public.reserva_noche WHERE id_reserva = ?",
                Integer.class,
                idReserva);
        return total == null ? 0 : total;
    }

    private void pasarelaAprueba() {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
    }

    private Callable<Integer> retencionAlMismoTiempo(CountDownLatch salida, UUID idUsuario, UUID idReserva) {
        String token = escenario.token(idUsuario);
        return () -> {
            salida.await();
            return mockMvc.perform(post("/reservas/{id}/retencion", idReserva)
                            .header("Authorization", token))
                    .andReturn().getResponse().getStatus();
        };
    }

    private MvcResult decidir(UUID idUsuario, UUID idReserva, String decision) throws Exception {
        return mockMvc.perform(patch("/reservas/{id}/" + decision, idReserva)
                        .header("Authorization", escenario.token(idUsuario)))
                .andReturn();
    }

    private Map<String, Object> operacion(UUID idReserva) {
        return jdbc.queryForMap(
                "SELECT estado, intentos FROM operacion_pendiente WHERE id_reserva = ?", idReserva);
    }

    private String estadoPago(UUID idReserva) {
        return jdbc.queryForObject("SELECT estado FROM pago WHERE id_reserva = ?", String.class, idReserva);
    }

    private Object retencionHasta(UUID idReserva) {
        return jdbc.queryForObject(
                "SELECT retencion_hasta FROM reserva WHERE id_reserva = ?", Object.class, idReserva);
    }

    private static <T> T campo(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), ruta);
    }
}
