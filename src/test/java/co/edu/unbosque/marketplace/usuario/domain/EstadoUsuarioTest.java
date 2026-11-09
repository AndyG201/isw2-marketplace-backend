package co.edu.unbosque.marketplace.usuario.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoUsuarioTest {

    @Test
    void contieneExactamenteLosTresEstadosEnOrden() {
        assertArrayEquals(
            new EstadoUsuario[]{
                EstadoUsuario.PENDIENTE_VERIFICACION,
                EstadoUsuario.ACTIVA,
                EstadoUsuario.INACTIVA
            },
            EstadoUsuario.values()
        );
    }

    @Test
    void estadosTienenLosNombresEsperados() {
        assertAll(
            () -> assertEquals(
                "PENDIENTE_VERIFICACION",
                EstadoUsuario.PENDIENTE_VERIFICACION.name()
            ),
            () -> assertEquals("ACTIVA", EstadoUsuario.ACTIVA.name()),
            () -> assertEquals("INACTIVA", EstadoUsuario.INACTIVA.name())
        );
    }

    @Test
    void obtieneCadaEstadoPorSuNombre() {
        assertAll(
            () -> assertSame(
                EstadoUsuario.PENDIENTE_VERIFICACION,
                EstadoUsuario.valueOf("PENDIENTE_VERIFICACION")
            ),
            () -> assertSame(
                EstadoUsuario.ACTIVA,
                EstadoUsuario.valueOf("ACTIVA")
            ),
            () -> assertSame(
                EstadoUsuario.INACTIVA,
                EstadoUsuario.valueOf("INACTIVA")
            )
        );
    }

    @Test
    void estadosTienenLasPosicionesEsperadas() {
        assertAll(
            () -> assertEquals(
                0, EstadoUsuario.PENDIENTE_VERIFICACION.ordinal()
            ),
            () -> assertEquals(1, EstadoUsuario.ACTIVA.ordinal()),
            () -> assertEquals(2, EstadoUsuario.INACTIVA.ordinal())
        );
    }

    @Test
    void toStringDevuelveElNombreDeCadaEstado() {
        assertAll(
            () -> assertEquals(
                "PENDIENTE_VERIFICACION",
                EstadoUsuario.PENDIENTE_VERIFICACION.toString()
            ),
            () -> assertEquals("ACTIVA", EstadoUsuario.ACTIVA.toString()),
            () -> assertEquals("INACTIVA", EstadoUsuario.INACTIVA.toString())
        );
    }

    @Test
    void rechazaNombresInvalidos() {
        for (String nombre : new String[]{
                "DESCONOCIDO", "", "activa", " ACTIVA "
        }) {
            assertThrows(
                IllegalArgumentException.class,
                () -> EstadoUsuario.valueOf(nombre),
                "Debe rechazar el nombre: " + nombre
            );
        }
    }

    @Test
    void rechazaNombreNulo() {
        assertThrows(
            NullPointerException.class,
            () -> EstadoUsuario.valueOf(null)
        );
    }

    @Test
    void modificarArregloDeValuesNoAlteraElEnum() {
        EstadoUsuario[] estados = EstadoUsuario.values();
        estados[0] = EstadoUsuario.INACTIVA;

        assertSame(
            EstadoUsuario.PENDIENTE_VERIFICACION,
            EstadoUsuario.values()[0]
        );
    }
}