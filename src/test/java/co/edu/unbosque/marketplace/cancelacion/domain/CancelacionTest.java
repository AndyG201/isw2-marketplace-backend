package co.edu.unbosque.marketplace.cancelacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CancelacionTest {

    private Cancelacion cancelacion;

    private UUID idCancelacion;
    private UUID idReserva;
    private UUID idUsuario;
    private OffsetDateTime fechaCancelacion;
    private BigDecimal valorDevolucion;

    @BeforeEach
    void setUp() {
        idCancelacion = UUID.randomUUID();
        idReserva = UUID.randomUUID();
        idUsuario = UUID.randomUUID();
        fechaCancelacion = OffsetDateTime.now();
        valorDevolucion = new BigDecimal("150000.00");

        cancelacion = new Cancelacion(
                idCancelacion,
                idReserva,
                idUsuario,
                fechaCancelacion,
                "Motivos personales",
                valorDevolucion,
                "PENDIENTE",
                "CANCELADA"
        );
    }

    @Test
    void constructorVacioCreaCancelacion() {
        Cancelacion nuevaCancelacion = new Cancelacion();

        assertNotNull(nuevaCancelacion);
    }

    @Test
    void constructorCompletoAsignaTodosLosAtributos() {
        assertEquals(idCancelacion, cancelacion.getIdCancelacion());
        assertEquals(idReserva, cancelacion.getIdReserva());
        assertEquals(idUsuario, cancelacion.getIdUsuario());
        assertEquals(fechaCancelacion, cancelacion.getFechaCancelacion());
        assertEquals("Motivos personales", cancelacion.getMotivo());
        assertEquals(0, valorDevolucion.compareTo(cancelacion.getValorDevolucion()));
        assertEquals("PENDIENTE", cancelacion.getEstadoDevolucion());
        assertEquals("CANCELADA", cancelacion.getEstadoFinal());
    }

    @Test
    void gettersRetornanLosValoresAsignados() {
        assertNotNull(cancelacion.getIdCancelacion());
        assertNotNull(cancelacion.getIdReserva());
        assertNotNull(cancelacion.getIdUsuario());
        assertNotNull(cancelacion.getFechaCancelacion());
        assertNotNull(cancelacion.getMotivo());
        assertNotNull(cancelacion.getValorDevolucion());
        assertNotNull(cancelacion.getEstadoDevolucion());
        assertNotNull(cancelacion.getEstadoFinal());
    }

    @Test
    void settersActualizanTodosLosAtributos() {
        UUID nuevoIdCancelacion = UUID.randomUUID();
        UUID nuevoIdReserva = UUID.randomUUID();
        UUID nuevoIdUsuario = UUID.randomUUID();
        OffsetDateTime nuevaFecha = OffsetDateTime.now().plusDays(1);
        BigDecimal nuevoValor = new BigDecimal("200000.00");

        cancelacion.setIdCancelacion(nuevoIdCancelacion);
        cancelacion.setIdReserva(nuevoIdReserva);
        cancelacion.setIdUsuario(nuevoIdUsuario);
        cancelacion.setFechaCancelacion(nuevaFecha);
        cancelacion.setMotivo("Cambio de planes");
        cancelacion.setValorDevolucion(nuevoValor);
        cancelacion.setEstadoDevolucion("APROBADA");
        cancelacion.setEstadoFinal("CANCELADA");

        assertEquals(nuevoIdCancelacion, cancelacion.getIdCancelacion());
        assertEquals(nuevoIdReserva, cancelacion.getIdReserva());
        assertEquals(nuevoIdUsuario, cancelacion.getIdUsuario());
        assertEquals(nuevaFecha, cancelacion.getFechaCancelacion());
        assertEquals("Cambio de planes", cancelacion.getMotivo());
        assertEquals(0, nuevoValor.compareTo(cancelacion.getValorDevolucion()));
        assertEquals("APROBADA", cancelacion.getEstadoDevolucion());
        assertEquals("CANCELADA", cancelacion.getEstadoFinal());
    }

    @Test
    void settersPermitenAsignarValoresNulos() {
        cancelacion.setIdCancelacion(null);
        cancelacion.setIdReserva(null);
        cancelacion.setIdUsuario(null);
        cancelacion.setFechaCancelacion(null);
        cancelacion.setMotivo(null);
        cancelacion.setValorDevolucion(null);
        cancelacion.setEstadoDevolucion(null);
        cancelacion.setEstadoFinal(null);

        assertNull(cancelacion.getIdCancelacion());
        assertNull(cancelacion.getIdReserva());
        assertNull(cancelacion.getIdUsuario());
        assertNull(cancelacion.getFechaCancelacion());
        assertNull(cancelacion.getMotivo());
        assertNull(cancelacion.getValorDevolucion());
        assertNull(cancelacion.getEstadoDevolucion());
        assertNull(cancelacion.getEstadoFinal());
    }
}
