package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryCuentasSinVerificarTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private VerificacionCorreoRepository verificacionCorreoRepository;
    @Autowired private TestEntityManager entityManager;

    private OffsetDateTime ahora;
    private Rol rol;

    @BeforeEach
    void configurar() {
        ahora = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        rol = new Rol(UUID.randomUUID(), "HUESPED", "Rol de pruebas");
        entityManager.persist(rol);
    }

    private Usuario guardar(String correo, byte identificador, EstadoUsuario estado,
            boolean verificado, OffsetDateTime fechaRegistro) {
        Usuario u = new Usuario();
        u.setIdUsuario(UUID.randomUUID());
        u.setRol(rol);
        u.setNombre("Ana");
        u.setApellido("Perez");
        u.setCorreo(correo);
        u.setTipoDocumento("CEDULA");
        u.setDocumentoCifrado(new byte[16]);
        u.setDocumentoNonce(new byte[12]);
        byte[] hash = new byte[32];
        hash[0] = identificador;
        u.setDocumentoHash(hash);
        u.setTelefono("300000000" + identificador);
        u.setContrasenaHash("hash-de-prueba");
        u.setCorreoVerificado(verificado);
        u.setEstado(estado);
        u.setRestringido(false);
        u.setIntentosFallidos(0);
        u.setFechaRegistro(fechaRegistro);
        u.setFechaActualizacion(fechaRegistro);
        entityManager.persist(u);
        return u;
    }

    private void guardarCodigo(Usuario usuario) {
        VerificacionCorreo verificacion = new VerificacionCorreo();
        verificacion.setUsuario(usuario);
        verificacion.setCodigoHash("hash-del-codigo");
        verificacion.setFechaExpiracion(ahora.minusHours(30));
        entityManager.persist(verificacion);
    }

    @Test
    void eliminaSoloLasCuentasPendientesAnterioresAlLimite() {
        Usuario vencida = guardar("vencida@ejemplo.com", (byte) 1,
                EstadoUsuario.PENDIENTE_VERIFICACION, false, ahora.minusHours(30));
        Usuario reciente = guardar("reciente@ejemplo.com", (byte) 2,
                EstadoUsuario.PENDIENTE_VERIFICACION, false, ahora.minusHours(2));
        Usuario activa = guardar("activa@ejemplo.com", (byte) 3,
                EstadoUsuario.ACTIVA, true, ahora.minusDays(10));
        Usuario inactiva = guardar("inactiva@ejemplo.com", (byte) 4,
                EstadoUsuario.INACTIVA, false, ahora.minusDays(10));
        entityManager.flush();
        entityManager.clear();

        int eliminadas = usuarioRepository.eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, ahora.minusHours(24));

        assertEquals(1, eliminadas);
        assertFalse(usuarioRepository.existsById(vencida.getIdUsuario()));
        assertTrue(usuarioRepository.existsById(reciente.getIdUsuario()));
        assertTrue(usuarioRepository.existsById(activa.getIdUsuario()));
        assertTrue(usuarioRepository.existsById(inactiva.getIdUsuario()));
    }

    @Test
    void borraTambienLosCodigosDeVerificacionDeLaCuentaEliminada() {
        Usuario vencida = guardar("vencida@ejemplo.com", (byte) 1,
                EstadoUsuario.PENDIENTE_VERIFICACION, false, ahora.minusHours(30));
        guardarCodigo(vencida);
        entityManager.flush();
        entityManager.clear();

        usuarioRepository.eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, ahora.minusHours(24));
        entityManager.clear();

        assertEquals(0, verificacionCorreoRepository.count());
    }

    @Test
    void liberaElCorreoParaUnNuevoRegistro() {
        guardar("vencida@ejemplo.com", (byte) 1,
                EstadoUsuario.PENDIENTE_VERIFICACION, false, ahora.minusHours(30));
        entityManager.flush();
        entityManager.clear();

        usuarioRepository.eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, ahora.minusHours(24));
        entityManager.clear();

        assertTrue(usuarioRepository.findByCorreoIgnoreCase("vencida@ejemplo.com").isEmpty());
        assertFalse(usuarioRepository.existsByTelefono("3000000001"));
    }
}
