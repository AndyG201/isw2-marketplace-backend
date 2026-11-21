
package co.edu.unbosque.marketplace.cancelacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;

import co.edu.unbosque.marketplace.cancelacion.api.response.CancelarReservaResponse;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.cancelacion.domain.Cancelacion;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.CancelacionRepository;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionGateway;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;

@ExtendWith(MockitoExtension.class)
class CancelacionReservaServiceTest {

    @Mock
    private ReservaCancelacionGateway reservas;

    @Mock
    private PoliticaCancelacionQuery politicas;

    @Mock
    private CancelacionRepository cancelaciones;

    @Mock
    private AuditoriaEventoCommand auditoria;

    private CancelacionReservaService servicio;

    private final UUID idReserva = UUID.randomUUID();
    private final UUID idHuesped = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();

    private static final BigDecimal MONTO =
            new BigDecimal("100000.00");

    @BeforeEach
    void setUp() {

        List<PoliticaDevolucionStrategy> estrategias = List.of(
                new DevolucionFlexibleStrategy(),
                new DevolucionModeradaStrategy(),
                new DevolucionEstrictaStrategy()
        );

        servicio = new CancelacionReservaService(
                reservas,
                politicas,
                cancelaciones,
                estrategias
        );

        servicio.configurarAuditoria(auditoria);
    }

    // =====================================================
    // PRUEBAS DE SELECCIÓN DE ESTRATEGIAS
    // =====================================================

    @Test
    void seleccionaEstrategiaFlexibleCorrectamente() {

        prepararCancelacion(
                "FLEXIBLE",
                24,
                "80",
                "PAGADO",
                MONTO
        );

        CancelarReservaResponse respuesta =
                servicio.cancelar(idHuesped, idReserva, "Cambio de planes");

        Cancelacion guardada = capturarCancelacion();

        assertNotNull(respuesta);

        assertEquals(
                new BigDecimal("80000.00"),
                guardada.getValorDevolucion()
        );

        assertEquals("PROCESADA", guardada.getEstadoDevolucion());

        verify(reservas).cancelarYLiberar(idReserva);
    }

    @Test
    void seleccionaEstrategiaModeradaCorrectamente() {

        prepararCancelacion(
                "MODERADA",
                120,
                "60",
                "PAGADO",
                MONTO
        );

        CancelarReservaResponse respuesta =
                servicio.cancelar(idHuesped, idReserva, "Cambio de planes");

        Cancelacion guardada = capturarCancelacion();

        assertNotNull(respuesta);

        assertEquals(
                new BigDecimal("60000.00"),
                guardada.getValorDevolucion()
        );

        assertEquals("PROCESADA", guardada.getEstadoDevolucion());

        verify(reservas).cancelarYLiberar(idReserva);
    }

    @Test
    void seleccionaEstrategiaEstrictaCorrectamente() {

        prepararCancelacion(
                "ESTRICTA",
                24,
                "100",
                "PAGADO",
                MONTO
        );

        CancelarReservaResponse respuesta =
                servicio.cancelar(idHuesped, idReserva, "Cambio de planes");

        Cancelacion guardada = capturarCancelacion();

        assertNotNull(respuesta);

        assertEquals(
                new BigDecimal("0.00"),
                guardada.getValorDevolucion()
        );

        assertEquals("NO_APLICA", guardada.getEstadoDevolucion());

        verify(reservas).cancelarYLiberar(idReserva);
    }

    // =====================================================
    // PRUEBAS DE VALIDACIÓN DE POLÍTICAS
    // =====================================================

    @Test
    void devuelveConflictoCuandoNoExisteEstrategiaCompatible() {

        prepararReserva("PERSONALIZADA", "PAGADO", MONTO);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(409, error.getStatusCode().value());

        verify(cancelaciones, never()).saveAndFlush(any(Cancelacion.class));
        verify(reservas, never()).cancelarYLiberar(idReserva);
    }

    @Test
    void devuelveConflictoCuandoLaPoliticaNoExiste() {

        prepararReserva("FLEXIBLE", "PAGADO", MONTO);

        when(politicas.consultarPorId(idPolitica))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(409, error.getStatusCode().value());

        verify(cancelaciones, never()).saveAndFlush(any(Cancelacion.class));
    }

    // =====================================================
    // PRUEBAS DE SEGURIDAD Y VALIDACIONES
    // =====================================================

    @Test
    void rechazaCancelacionCuandoElHuespedEsNulo() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        null,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(401, error.getStatusCode().value());

        verifyNoInteractions(reservas, politicas, cancelaciones);
    }

    @Test
    void rechazaCancelacionCuandoLaReservaNoPerteneceAlHuesped() {

        ReservaCancelacionInfo reserva = crearReserva(
                UUID.randomUUID(),
                "CONFIRMADA",
                "PAGADO",
                MONTO
        );

        when(reservas.bloquearYConsultar(idReserva))
                .thenReturn(Optional.of(reserva));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(403, error.getStatusCode().value());

        verifyNoInteractions(politicas, cancelaciones);
        verify(reservas, never()).cancelarYLiberar(idReserva);
    }

    @Test
    void devuelveErrorCuandoLaReservaNoExiste() {

        when(reservas.bloquearYConsultar(idReserva))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void rechazaMotivoVacio() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        " "
                )
        );

        assertEquals(400, error.getStatusCode().value());

        verifyNoInteractions(reservas, politicas, cancelaciones);
    }

    // =====================================================
    // PRUEBAS DE ESTADOS Y PAGOS
    // =====================================================

    @Test
    void rechazaReservaConPagoPendiente() {

        prepararReserva("FLEXIBLE", "PENDIENTE", MONTO);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(409, error.getStatusCode().value());

        verifyNoInteractions(politicas);
        verify(cancelaciones, never()).saveAndFlush(any(Cancelacion.class));
    }

    @Test
    void rechazaReservaConEstadoNoCancelable() {

        prepararReservaConEstado(
                "COMPLETADA",
                "PAGADO",
                MONTO
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Cambio de planes"
                )
        );

        assertEquals(409, error.getStatusCode().value());

        verifyNoInteractions(politicas);
        verify(cancelaciones, never()).saveAndFlush(any(Cancelacion.class));
    }

    // =====================================================
    // PRUEBAS DE IDEMPOTENCIA
    // =====================================================

    @Test
    void noDuplicaUnaCancelacionExistente() {

        Cancelacion existente = new Cancelacion(
                UUID.randomUUID(),
                idReserva,
                idHuesped,
                OffsetDateTime.now(ZoneOffset.UTC),
                "Cancelación anterior",
                new BigDecimal("50000.00"),
                "PROCESADA",
                "CANCELADA"
        );

        when(reservas.bloquearYConsultar(idReserva))
                .thenReturn(Optional.of(
                        crearReserva(
                                idHuesped,
                                "CONFIRMADA",
                                "PAGADO",
                                MONTO
                        )
                ));

        when(cancelaciones.findByIdReserva(idReserva))
                .thenReturn(Optional.of(existente));

        CancelarReservaResponse respuesta =
                servicio.cancelar(
                        idHuesped,
                        idReserva,
                        "Nueva solicitud"
                );

        assertNotNull(respuesta);

        verify(cancelaciones, never()).saveAndFlush(any(Cancelacion.class));
        verifyNoInteractions(politicas, auditoria);

        verify(reservas, never()).cancelarYLiberar(idReserva);
    }

    // =====================================================
    // PRUEBAS DE AUDITORÍA
    // =====================================================

    @Test
    void registraEventoDeAuditoriaAlCancelarReserva() {

        prepararCancelacion(
                "FLEXIBLE",
                24,
                "80",
                "PAGADO",
                MONTO
        );

        servicio.cancelar(
                idHuesped,
                idReserva,
                "Solicitud de cancelación"
        );

        verify(auditoria).registrarEvento(
                any(RegistrarEventoAuditoria.class)
        );

        verify(reservas).cancelarYLiberar(idReserva);
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private void prepararCancelacion(
            String nombrePolitica,
            Integer anticipacion,
            String porcentaje,
            String estadoPago,
            BigDecimal montoPagado) {

        prepararReserva(nombrePolitica, estadoPago, montoPagado);

        when(cancelaciones.findByIdReserva(idReserva))
                .thenReturn(Optional.empty());

        PoliticaCancelacionInfo politica =
                new PoliticaCancelacionInfo(
                        idPolitica,
                        nombrePolitica,
                        1,
                        "Política de prueba",
                        anticipacion,
                        new BigDecimal(porcentaje),
                        true
                );

        when(politicas.consultarPorId(idPolitica))
                .thenReturn(Optional.of(politica));
    }

    private void prepararReserva(
            String nombrePolitica,
            String estadoPago,
            BigDecimal montoPagado) {

        ReservaCancelacionInfo reserva = crearReserva(
                idHuesped,
                "CONFIRMADA",
                estadoPago,
                montoPagado
        );

        when(reservas.bloquearYConsultar(idReserva))
                .thenReturn(Optional.of(reserva));
    }

    private void prepararReservaConEstado(
            String estado,
            String estadoPago,
            BigDecimal montoPagado) {

        ReservaCancelacionInfo reserva = crearReserva(
                idHuesped,
                estado,
                estadoPago,
                montoPagado
        );

        when(reservas.bloquearYConsultar(idReserva))
                .thenReturn(Optional.of(reserva));

        when(cancelaciones.findByIdReserva(idReserva))
                .thenReturn(Optional.empty());
    }

    private ReservaCancelacionInfo crearReserva(
            UUID huesped,
            String estado,
            String estadoPago,
            BigDecimal montoPagado) {

        return new ReservaCancelacionInfo(
                idReserva,
                huesped,
                estado,
                idPolitica,
                LocalDate.now(ZoneOffset.UTC).plusDays(10),
                MONTO,
                montoPagado,
                estadoPago
        );
    }

    private Cancelacion capturarCancelacion() {

        ArgumentCaptor<Cancelacion> captor =
                ArgumentCaptor.forClass(Cancelacion.class);

        verify(cancelaciones).saveAndFlush(captor.capture());

        return captor.getValue();
    }
}
