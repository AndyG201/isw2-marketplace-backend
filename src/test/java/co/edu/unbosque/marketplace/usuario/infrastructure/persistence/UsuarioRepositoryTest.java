package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.util.UUID;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario ana;
    private Usuario carlos;

    @BeforeEach
    void configurar() {
        Rol rol = new Rol(UUID.randomUUID(), "HUESPED", "Rol de pruebas");
        entityManager.persist(rol);

        ana = crearUsuario(rol, "Ana", "Ana@ejemplo.com", "3001234567", (byte) 1);
        carlos = crearUsuario(rol, "Carlos", "carlos@ejemplo.com", "3007654321", (byte) 2);

        entityManager.persist(ana);
        entityManager.persist(carlos);
        entityManager.flush();
        entityManager.clear();
    }

    private Usuario crearUsuario(
            Rol rol, String nombre, String correo, String telefono, byte identificador) {

        Usuario u = new Usuario();
        u.setIdUsuario(UUID.randomUUID());
        u.setRol(rol);
        u.setNombre(nombre);
        u.setApellido("Perez");
        u.setCorreo(correo);
        u.setTipoDocumento("CEDULA");
        u.setDocumentoCifrado(new byte[16]);
        u.setDocumentoNonce(new byte[12]);
        byte[] hash = new byte[32];
        hash[0] = identificador;
        u.setDocumentoHash(hash);
        u.setTelefono(telefono);
        u.setContrasenaHash("hash-de-prueba");
        u.setCorreoVerificado(true);
        u.setEstado(EstadoUsuario.ACTIVA);
        u.setRestringido(false);
        u.setIntentosFallidos(0);
        OffsetDateTime ahora = OffsetDateTime.now();
        u.setFechaRegistro(ahora);
        u.setFechaActualizacion(ahora);
        return u;
    }

    @Test
    void findByCorreoEncuentraUsuarioConCorreoExacto() {
        var resultado = usuarioRepository.findByCorreo("Ana@ejemplo.com");

        assertTrue(resultado.isPresent());
        Usuario encontrado = resultado.orElseThrow();
        assertAll(
                () -> assertEquals(ana.getIdUsuario(), encontrado.getIdUsuario()),
                () -> assertEquals("Ana", encontrado.getNombre()),
                () -> assertEquals("Ana@ejemplo.com", encontrado.getCorreo()));
    }

    @Test
    void findByCorreoRetornaVacioSiNoExiste() {
        assertTrue(usuarioRepository.findByCorreo("nadie@ejemplo.com").isEmpty());
    }

    @Test
    void findByCorreoDistingueMayusculas() {
        assertTrue(usuarioRepository.findByCorreo("ana@ejemplo.com").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ana@ejemplo.com", "ana@ejemplo.com", "ANA@EJEMPLO.COM"})
    void findByCorreoIgnoreCaseEncuentraSinDistinguirMayusculas(String correo) {
        var resultado = usuarioRepository.findByCorreoIgnoreCase(correo);

        assertTrue(resultado.isPresent());
        assertEquals(ana.getIdUsuario(), resultado.orElseThrow().getIdUsuario());
    }

    @Test
    void findByCorreoIgnoreCaseRetornaVacioSiNoExiste() {
        assertTrue(usuarioRepository.findByCorreoIgnoreCase("nadie@ejemplo.com").isEmpty());
    }

    @Test
    void existsByDocumentoHashRetornaTrueParaMismoContenido() {
        // Un arreglo diferente con los mismos bytes debe encontrar el documento.
        byte[] copiaHash = ana.getDocumentoHash().clone();

        assertNotSame(ana.getDocumentoHash(), copiaHash);
        assertTrue(usuarioRepository.existsByDocumentoHash(copiaHash));
    }

    @Test
    void existsByDocumentoHashRetornaFalseSiNoExiste() {
        byte[] hashDesconocido = new byte[32];
        hashDesconocido[0] = 99;

        assertFalse(usuarioRepository.existsByDocumentoHash(hashDesconocido));
    }

    @Test
    void existsByTelefonoRetornaTrueSiExiste() {
        assertTrue(usuarioRepository.existsByTelefono("3001234567"));
    }

    @Test
    void existsByTelefonoRetornaFalseSiNoExiste() {
        assertFalse(usuarioRepository.existsByTelefono("3999999999"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ana@ejemplo.com", "ana@ejemplo.com", "ANA@EJEMPLO.COM"})
    void existsByCorreoExcluyendoIdRetornaTrueSiCorreoPerteneceAOtroUsuario(String correo) {
        assertTrue(usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(
                correo, carlos.getIdUsuario()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ana@ejemplo.com", "ana@ejemplo.com", "ANA@EJEMPLO.COM"})
    void existsByCorreoExcluyendoIdRetornaFalseParaElPropioUsuario(String correo) {
        assertFalse(usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(
                correo, ana.getIdUsuario()));
    }

    @Test
    void existsByCorreoExcluyendoIdRetornaFalseSiCorreoNoExiste() {
        assertFalse(usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(
                "nadie@ejemplo.com", ana.getIdUsuario()));
    }
}


