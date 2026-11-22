package co.edu.unbosque.marketplace.reserva.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

import static co.edu.unbosque.marketplace.reserva.contracts.EstadoReserva.*;

class EstadoReservaTest {

    @Test
    void tablaDeTransicionesPermitidas() {
        assertEquals(Set.of(PENDIENTE_PAGO, RECHAZADA, CANCELADA), PENDIENTE_APROBACION.transicionesPermitidas());
        assertEquals(Set.of(CONFIRMADA, EXPIRADA, CANCELADA), PENDIENTE_PAGO.transicionesPermitidas());
        assertEquals(Set.of(COMPLETADA, CANCELADA), CONFIRMADA.transicionesPermitidas());
        for (EstadoReserva finalizado : List.of(RECHAZADA, CANCELADA, COMPLETADA, EXPIRADA)) {
            assertTrue(finalizado.transicionesPermitidas().isEmpty(), finalizado.name());
            assertTrue(finalizado.esFinal(), finalizado.name());
        }
        assertFalse(CONFIRMADA.esFinal());
    }

    @ParameterizedTest
    @EnumSource(EstadoReserva.class)
    void ningunEstadoPasaASiMismoYSoloSeAvanzaPorLaTabla(EstadoReserva origen) {
        assertFalse(origen.puedePasarA(origen));
        assertFalse(origen.puedePasarA(null));
        for (EstadoReserva destino : EstadoReserva.values()) {
            assertEquals(origen.transicionesPermitidas().contains(destino), origen.puedePasarA(destino));
        }
    }

    @Test
    void transicionarADevuelveElDestinoOLanzaConflicto() {
        assertSame(CONFIRMADA, PENDIENTE_PAGO.transicionarA(CONFIRMADA));

        ConflictoNegocioException error =
                assertThrows(ConflictoNegocioException.class, () -> COMPLETADA.transicionarA(CONFIRMADA));
        assertEquals("Una reserva en estado COMPLETADA no puede pasar a CONFIRMADA.", error.getMessage());
        assertThrows(ConflictoNegocioException.class, () -> PENDIENTE_PAGO.transicionarA(null));
        assertThrows(ConflictoNegocioException.class, () -> PENDIENTE_APROBACION.transicionarA(CONFIRMADA));
    }

    @Test
    void desdeConvierteElTextoDeLaBaseDeDatosYRechazaLosDesconocidos() {
        for (EstadoReserva estado : EstadoReserva.values()) {
            assertSame(estado, EstadoReserva.desde(estado.name()));
        }
        assertThrows(FallaInternaException.class, () -> EstadoReserva.desde("INVENTADA"));
        assertThrows(FallaInternaException.class, () -> EstadoReserva.desde(null));
        assertThrows(FallaInternaException.class, () -> EstadoReserva.desde("confirmada"));
    }

    @Test
    void losConjuntosDeNegocioSonLosEsperados() {
        assertEquals(EnumSet.of(PENDIENTE_PAGO, PENDIENTE_APROBACION, CONFIRMADA), EstadoReserva.CANCELABLES);
        assertEquals(EstadoReserva.CANCELABLES, EstadoReserva.VIGENTES);
        assertEquals(EnumSet.of(CONFIRMADA, COMPLETADA), EstadoReserva.QUE_OCUPAN_NOCHES);
        assertEquals(EnumSet.of(PENDIENTE_APROBACION, PENDIENTE_PAGO), EstadoReserva.CON_RETENCION);
        assertEquals(EnumSet.of(RECHAZADA, CANCELADA, EXPIRADA), EstadoReserva.QUE_LIBERAN_NOCHES);
        // Lo que se puede cancelar nunca es un estado final, y lo que libera noches siempre lo es.
        EstadoReserva.CANCELABLES.forEach(estado -> assertFalse(estado.esFinal()));
        EstadoReserva.QUE_LIBERAN_NOCHES.forEach(estado -> assertTrue(estado.esFinal()));
        // Cada estado cuenta exactamente en uno de los grupos de noches (ocupa, retiene o libera) o es CONFIRMADA/COMPLETADA.
        for (EstadoReserva estado : EstadoReserva.values()) {
            int grupos = (EstadoReserva.QUE_OCUPAN_NOCHES.contains(estado) ? 1 : 0)
                    + (EstadoReserva.CON_RETENCION.contains(estado) ? 1 : 0)
                    + (EstadoReserva.QUE_LIBERAN_NOCHES.contains(estado) ? 1 : 0);
            assertEquals(1, grupos, estado.name());
        }
    }

    @Test
    void generaNombres() {
        assertEquals(List.of("PENDIENTE_PAGO", "PENDIENTE_APROBACION", "CONFIRMADA", "RECHAZADA", "CANCELADA",
                "COMPLETADA", "EXPIRADA"), EstadoReserva.nombres());
        assertEquals(Set.of("CONFIRMADA", "COMPLETADA"), EstadoReserva.nombres(EstadoReserva.QUE_OCUPAN_NOCHES));
    }

    @ParameterizedTest
    @EnumSource(EstadoReserva.class)
    void laEntidadSoloCambiaDeEstadoPorLaTabla(EstadoReserva origen) {
        for (EstadoReserva destino : EstadoReserva.values()) {
            Reserva reserva = reservaEn(origen.name());
            if (origen.puedePasarA(destino)) {
                reserva.cambiarEstado(destino);
                assertEquals(destino.name(), reserva.getEstado());
                assertSame(destino, reserva.estadoActual());
            } else {
                assertThrows(ConflictoNegocioException.class, () -> reserva.cambiarEstado(destino));
                assertEquals(origen.name(), reserva.getEstado(), "el estado no debe cambiar si la transición es inválida");
            }
        }
    }

    @Test
    void laEntidadConsultaSuEstadoSinFallarConValoresRaros() {
        Reserva reserva = reservaEn("CONFIRMADA");
        assertTrue(reserva.estaEn(CONFIRMADA));
        assertTrue(reserva.estaEn(PENDIENTE_PAGO, CONFIRMADA));
        assertFalse(reserva.estaEn(PENDIENTE_PAGO, CANCELADA));
        assertFalse(reserva.estaEn());

        Reserva sinEstado = reservaEn(null);
        assertFalse(sinEstado.estaEn(CONFIRMADA));
        assertThrows(FallaInternaException.class, sinEstado::estadoActual);
        assertThrows(FallaInternaException.class, () -> sinEstado.cambiarEstado(CONFIRMADA));
    }

    private static Reserva reservaEn(String estado) {
        return new Reserva(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "INMEDIATA",
                estado, null, "clave", false, OffsetDateTime.now(), null);
    }
}
