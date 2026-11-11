package co.edu.unbosque.marketplace.usuario.domain;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolTest {

    @Test
    void constructorVacioInicializaCamposEnNull() {
        Rol rol = new Rol();

        assertAll(
            () -> assertNull(rol.getIdRol()),
            () -> assertNull(rol.getNombre()),
            () -> assertNull(rol.getDescripcion())
        );
    }

    @Test
    void constructorConParametrosAsignaTodosLosCampos() {
        UUID id = UUID.randomUUID();

        Rol rol = new Rol(
            id,
            "HUESPED",
            "Usuario que puede realizar reservas."
        );

        assertAll(
            () -> assertEquals(id, rol.getIdRol()),
            () -> assertEquals("HUESPED", rol.getNombre()),
            () -> assertEquals(
                "Usuario que puede realizar reservas.",
                rol.getDescripcion()
            )
        );
    }

    @Test
    void constructorConParametrosPermiteValoresNulos() {
        Rol rol = new Rol(null, null, null);

        assertAll(
            () -> assertNull(rol.getIdRol()),
            () -> assertNull(rol.getNombre()),
            () -> assertNull(rol.getDescripcion())
        );
    }

    @Test
    void setIdRolActualizaIdentificador() {
        Rol rol = new Rol(UUID.randomUUID(), "HUESPED", "Descripción");
        UUID nuevoId = UUID.randomUUID();

        rol.setIdRol(nuevoId);

        assertEquals(nuevoId, rol.getIdRol());
    }

    @Test
    void setNombreActualizaNombre() {
        Rol rol = new Rol(UUID.randomUUID(), "HUESPED", "Descripción");

        rol.setNombre("ANFITRION");

        assertEquals("ANFITRION", rol.getNombre());
    }

    @Test
    void setDescripcionActualizaDescripcion() {
        Rol rol = new Rol(
            UUID.randomUUID(),
            "ANFITRION",
            "Descripción anterior"
        );

        rol.setDescripcion("Usuario que puede publicar alojamientos.");

        assertEquals(
            "Usuario que puede publicar alojamientos.",
            rol.getDescripcion()
        );
    }

    @Test
    void settersPermitenAsignarValoresNulos() {
        Rol rol = new Rol(
            UUID.randomUUID(),
            "HUESPED",
            "Descripción"
        );

        rol.setIdRol(null);
        rol.setNombre(null);
        rol.setDescripcion(null);

        assertAll(
            () -> assertNull(rol.getIdRol()),
            () -> assertNull(rol.getNombre()),
            () -> assertNull(rol.getDescripcion())
        );
    }

    @Test
    void settersConservanCadenasVacias() {
        Rol rol = new Rol();

        rol.setNombre("");
        rol.setDescripcion("");

        assertAll(
            () -> assertEquals("", rol.getNombre()),
            () -> assertEquals("", rol.getDescripcion())
        );
    }
}
