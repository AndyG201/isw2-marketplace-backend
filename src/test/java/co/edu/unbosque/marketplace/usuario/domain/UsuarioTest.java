package co.edu.unbosque.marketplace.usuario.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void constructorInicializaValoresPorDefecto() {
        Usuario u = new Usuario();

        assertAll(
                () -> assertNull(u.getIdUsuario()),
                () -> assertNull(u.getRol()),
                () -> assertNull(u.getNombre()),
                () -> assertNull(u.getApellido()),
                () -> assertNull(u.getCorreo()),
                () -> assertNull(u.getTipoDocumento()),
                () -> assertNull(u.getDocumentoCifrado()),
                () -> assertNull(u.getDocumentoNonce()),
                () -> assertNull(u.getDocumentoHash()),
                () -> assertNull(u.getTelefono()),
                () -> assertNull(u.getContrasenaHash()),
                () -> assertFalse(u.isCorreoVerificado()),
                () -> assertEquals(EstadoUsuario.PENDIENTE_VERIFICACION, u.getEstado()),
                () -> assertFalse(u.isRestringido()),
                () -> assertEquals(0, u.getIntentosFallidos()),
                () -> assertNull(u.getMotivoRestriccion()),
                () -> assertNull(u.getMotivoBloqueo()),
                () -> assertNull(u.getFechaRegistro()),
                () -> assertNull(u.getFechaActualizacion()));
    }

    private Usuario usuarioConDatos() {
        Usuario u = new Usuario();
        u.setIdUsuario(UUID.fromString("47af4392-4123-4839-95ad-6ff38f995ea1"));
        u.setRol(new Rol(UUID.randomUUID(), "HUESPED", "Puede reservar"));
        u.setNombre("Ana");
        u.setApellido("Perez");
        u.setCorreo("ana@ejemplo.com");
        u.setTipoDocumento("CEDULA");
        u.setDocumentoCifrado(new byte[]{1, 2});
        u.setDocumentoNonce(new byte[]{3, 4});
        u.setDocumentoHash(new byte[]{5, 6});
        u.setTelefono("3001234567");
        u.setContrasenaHash("hash-seguro");
        u.setCorreoVerificado(true);
        u.setEstado(EstadoUsuario.ACTIVA);
        u.setRestringido(true);
        u.setIntentosFallidos(5);
        u.setMotivoRestriccion("Incumplimiento");
        u.setMotivoBloqueo("Bloqueo automático por intentos fallidos");
        u.setFechaRegistro(OffsetDateTime.parse("2026-01-01T10:00:00-05:00"));
        u.setFechaActualizacion(OffsetDateTime.parse("2026-01-02T11:00:00-05:00"));
        return u;
    }

    @Test
    void gettersDevuelvenTodosLosValoresAsignados() {
        Usuario u = usuarioConDatos();

        assertAll(
                () -> assertEquals(UUID.fromString("47af4392-4123-4839-95ad-6ff38f995ea1"),
                        u.getIdUsuario()),
                () -> assertEquals("HUESPED", u.getRol().getNombre()),
                () -> assertEquals("Ana", u.getNombre()),
                () -> assertEquals("Perez", u.getApellido()),
                () -> assertEquals("ana@ejemplo.com", u.getCorreo()),
                () -> assertEquals("CEDULA", u.getTipoDocumento()),
                () -> assertArrayEquals(new byte[]{1, 2}, u.getDocumentoCifrado()),
                () -> assertArrayEquals(new byte[]{3, 4}, u.getDocumentoNonce()),
                () -> assertArrayEquals(new byte[]{5, 6}, u.getDocumentoHash()),
                () -> assertEquals("3001234567", u.getTelefono()),
                () -> assertEquals("hash-seguro", u.getContrasenaHash()),
                () -> assertTrue(u.isCorreoVerificado()),
                () -> assertEquals(EstadoUsuario.ACTIVA, u.getEstado()),
                () -> assertTrue(u.isRestringido()),
                () -> assertEquals(5, u.getIntentosFallidos()),
                () -> assertEquals("Incumplimiento", u.getMotivoRestriccion()),
                () -> assertEquals("Bloqueo automático por intentos fallidos", u.getMotivoBloqueo()),
                () -> assertEquals(OffsetDateTime.parse("2026-01-01T10:00:00-05:00"),
                        u.getFechaRegistro()),
                () -> assertEquals(OffsetDateTime.parse("2026-01-02T11:00:00-05:00"),
                        u.getFechaActualizacion()));
    }

    @Test
    void settersPermitenLimpiarCamposDeReferencia() {
        Usuario u = usuarioConDatos();
        u.setIdUsuario(null);
        u.setRol(null);
        u.setNombre(null);
        u.setApellido(null);
        u.setCorreo(null);
        u.setTipoDocumento(null);
        u.setDocumentoCifrado(null);
        u.setDocumentoNonce(null);
        u.setDocumentoHash(null);
        u.setTelefono(null);
        u.setContrasenaHash(null);
        u.setEstado(null);
        u.setMotivoRestriccion(null);
        u.setMotivoBloqueo(null);
        u.setFechaRegistro(null);
        u.setFechaActualizacion(null);

        assertAll(
                () -> assertNull(u.getIdUsuario()),
                () -> assertNull(u.getRol()),
                () -> assertNull(u.getNombre()),
                () -> assertNull(u.getApellido()),
                () -> assertNull(u.getCorreo()),
                () -> assertNull(u.getTipoDocumento()),
                () -> assertNull(u.getDocumentoCifrado()),
                () -> assertNull(u.getDocumentoNonce()),
                () -> assertNull(u.getDocumentoHash()),
                () -> assertNull(u.getTelefono()),
                () -> assertNull(u.getContrasenaHash()),
                () -> assertNull(u.getEstado()),
                () -> assertNull(u.getMotivoRestriccion()),
                () -> assertNull(u.getMotivoBloqueo()),
                () -> assertNull(u.getFechaRegistro()),
                () -> assertNull(u.getFechaActualizacion()));
    }

    @Test
    void permiteReiniciarBooleanosEIntentosFallidos() {
        Usuario u = usuarioConDatos();
        u.setCorreoVerificado(false);
        u.setRestringido(false);
        u.setIntentosFallidos(0);

        assertAll(
                () -> assertFalse(u.isCorreoVerificado()),
                () -> assertFalse(u.isRestringido()),
                () -> assertEquals(0, u.getIntentosFallidos()));
    }

    @ParameterizedTest
    @EnumSource(EstadoUsuario.class)
    void permiteAsignarCadaEstado(EstadoUsuario estado) {
        Usuario u = new Usuario();
        u.setEstado(estado);
        assertSame(estado, u.getEstado());
    }

    @Test
    void usernameYPasswordDevuelvenCorreoYHashActuales() {
        Usuario u = new Usuario();
        assertNull(u.getUsername());
        assertNull(u.getPassword());

        u.setCorreo("nuevo@ejemplo.com");
        u.setContrasenaHash("nuevo-hash");

        assertEquals("nuevo@ejemplo.com", u.getUsername());
        assertEquals("nuevo-hash", u.getPassword());
    }

    @Test
    void authoritiesEstaVacioCuandoNoHayRol() {
        Usuario u = new Usuario();
        assertTrue(u.getAuthorities().isEmpty());
    }

    @Test
    void authoritiesEstaVacioCuandoRolNoTieneNombre() {
        Usuario u = new Usuario();
        u.setRol(new Rol(UUID.randomUUID(), null, "Sin nombre"));
        assertTrue(u.getAuthorities().isEmpty());
    }

    static Stream<Arguments> rolesYAutoridades() {
        return Stream.of(
                Arguments.of("HUESPED", "ROLE_HUESPED"),
                Arguments.of("anfitrion", "ROLE_ANFITRION"),
                Arguments.of("  HuEsPeD  ", "ROLE_HUESPED"),
                Arguments.of("ADMIN", "ROLE_ADMIN"),
                Arguments.of("", "ROLE_"),
                Arguments.of("   ", "ROLE_"));
    }

    @ParameterizedTest
    @MethodSource("rolesYAutoridades")
    void authoritiesAgregaPrefijoYNormalizaNombre(String nombre, String esperado) {
        Usuario u = new Usuario();
        u.setRol(new Rol(UUID.randomUUID(), nombre, "Descripción"));

        var autoridades = u.getAuthorities();

        assertEquals(1, autoridades.size());
        assertEquals(esperado, autoridades.iterator().next().getAuthority());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void accountNonLockedDependeDeLaRestriccion(boolean restringido) {
        Usuario u = new Usuario();
        u.setRestringido(restringido);
        assertEquals(!restringido, u.isAccountNonLocked());
    }

    @Test
    void accountNonLockedNoEvaluaIntentosFallidos() {
        Usuario u = new Usuario();
        u.setIntentosFallidos(5);
        u.setMotivoBloqueo("Bloqueo automático por intentos fallidos");
        u.setRestringido(false);

        // Este método solo consulta la restricción administrativa.
        assertTrue(u.isAccountNonLocked());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void enabledDependeDelCorreoVerificado(boolean verificado) {
        Usuario u = new Usuario();
        u.setCorreoVerificado(verificado);
        assertEquals(verificado, u.isEnabled());
    }

    @ParameterizedTest
    @EnumSource(EstadoUsuario.class)
    void enabledNoEvaluaElEstadoNiLaRestriccion(EstadoUsuario estado) {
        Usuario u = new Usuario();
        u.setEstado(estado);
        u.setRestringido(true);
        u.setCorreoVerificado(true);
        assertTrue(u.isEnabled());
    }

    @Test
    void cuentaYCredencialesNoExpiran() {
        Usuario u = usuarioConDatos();

        assertAll(
                () -> assertTrue(u.isAccountNonExpired()),
                () -> assertTrue(u.isCredentialsNonExpired()));
    }
}