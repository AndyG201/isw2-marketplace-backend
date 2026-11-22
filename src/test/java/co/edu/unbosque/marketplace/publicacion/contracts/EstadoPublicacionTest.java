package co.edu.unbosque.marketplace.publicacion.contracts;

import static co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion.ACTIVA;
import static co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion.BLOQUEADA;
import static co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion.BORRADOR;
import static co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion.PAUSADA;
import static co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion.PENDIENTE_REVISION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

class EstadoPublicacionTest {

    @Test
    void tablaDeTransicionesPermitidas() {
        assertEquals(Set.of(PENDIENTE_REVISION, BLOQUEADA), BORRADOR.transicionesPermitidas());
        assertEquals(Set.of(ACTIVA, PAUSADA, BORRADOR, BLOQUEADA), PENDIENTE_REVISION.transicionesPermitidas());
        assertEquals(Set.of(PAUSADA, PENDIENTE_REVISION, BLOQUEADA), ACTIVA.transicionesPermitidas());
        assertEquals(Set.of(ACTIVA, PENDIENTE_REVISION, BLOQUEADA), PAUSADA.transicionesPermitidas());
        // Una publicación bloqueada solo se desbloquea hacia PAUSADA; el anfitrión decide cuándo reactivarla.
        assertEquals(Set.of(PAUSADA), BLOQUEADA.transicionesPermitidas());
    }

    @ParameterizedTest
    @EnumSource(EstadoPublicacion.class)
    void ningunEstadoPasaASiMismoYSoloSeAvanzaPorLaTabla(EstadoPublicacion origen) {
        assertFalse(origen.puedePasarA(origen));
        assertFalse(origen.puedePasarA(null));
        for (EstadoPublicacion destino : EstadoPublicacion.values()) {
            assertEquals(origen.transicionesPermitidas().contains(destino), origen.puedePasarA(destino));
        }
    }

    @Test
    void unaPublicacionNuncaSaltaDeBloqueadaAActivaNiDeBorradorAActiva() {
        assertFalse(BLOQUEADA.puedePasarA(ACTIVA));
        assertFalse(BORRADOR.puedePasarA(ACTIVA));
        assertFalse(BORRADOR.puedePasarA(PAUSADA));
    }

    @Test
    void transicionarADevuelveElDestinoOLanzaConflicto() {
        assertSame(ACTIVA, PENDIENTE_REVISION.transicionarA(ACTIVA));

        ConflictoNegocioException error =
                assertThrows(ConflictoNegocioException.class, () -> BLOQUEADA.transicionarA(ACTIVA));
        assertEquals("Una publicación en estado BLOQUEADA no puede pasar a ACTIVA.", error.getMessage());
        assertThrows(ConflictoNegocioException.class, () -> ACTIVA.transicionarA(null));
    }

    @Test
    void desdeConvierteElTextoDeLaBaseDeDatosYRechazaLosDesconocidos() {
        for (EstadoPublicacion estado : EstadoPublicacion.values()) {
            assertSame(estado, EstadoPublicacion.desde(estado.name()));
        }
        assertThrows(FallaInternaException.class, () -> EstadoPublicacion.desde("INVENTADA"));
        assertThrows(FallaInternaException.class, () -> EstadoPublicacion.desde(null));
        assertThrows(FallaInternaException.class, () -> EstadoPublicacion.desde("activa"));
    }

    @Test
    void losConjuntosDeNegocioSonLosEsperados() {
        assertEquals(EnumSet.of(ACTIVA, PAUSADA), EstadoPublicacion.APROBADAS);
        assertEquals(EnumSet.of(PENDIENTE_REVISION, BLOQUEADA), EstadoPublicacion.SIN_EDICION);
        assertEquals(Set.of("ACTIVA", "PAUSADA"), EstadoPublicacion.nombres(EstadoPublicacion.APROBADAS));
        assertEquals(Set.of("PENDIENTE_REVISION", "BLOQUEADA"), EstadoPublicacion.nombres(EstadoPublicacion.SIN_EDICION));
    }

    @ParameterizedTest
    @EnumSource(EstadoPublicacion.class)
    void laEntidadSoloCambiaDeEstadoPorLaTabla(EstadoPublicacion origen) {
        for (EstadoPublicacion destino : EstadoPublicacion.values()) {
            Publicacion publicacion = publicacionEn(origen.name());
            if (origen.puedePasarA(destino)) {
                publicacion.cambiarEstado(destino);
                assertEquals(destino.name(), publicacion.getEstado());
                assertSame(destino, publicacion.estadoActual());
            } else {
                assertThrows(ConflictoNegocioException.class, () -> publicacion.cambiarEstado(destino));
                assertEquals(origen.name(), publicacion.getEstado());
            }
        }
    }

    @Test
    void laEntidadConsultaSuEstadoSinFallarConValoresRaros() {
        Publicacion publicacion = publicacionEn("ACTIVA");
        assertTrue(publicacion.estaEn(ACTIVA));
        assertTrue(publicacion.estaEn(PAUSADA, ACTIVA));
        assertFalse(publicacion.estaEn(BLOQUEADA));
        assertFalse(publicacion.estaEn());

        Publicacion sinEstado = publicacionEn(null);
        assertFalse(sinEstado.estaEn(ACTIVA));
        assertThrows(FallaInternaException.class, sinEstado::estadoActual);
        assertThrows(FallaInternaException.class, () -> sinEstado.cambiarEstado(ACTIVA));
    }

    private static Publicacion publicacionEn(String estado) {
        return new Publicacion(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Titulo", "Descripcion",
                "Reglas", new java.math.BigDecimal("100000"), java.math.BigDecimal.ZERO, "INMEDIATA", estado,
                OffsetDateTime.now(), OffsetDateTime.now());
    }
}
