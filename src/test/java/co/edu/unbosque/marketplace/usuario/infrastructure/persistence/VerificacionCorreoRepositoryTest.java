package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VerificacionCorreoRepositoryTest {

    private static final String ROL_HUESPED = "HUESPED";
    private static final String CORREO_ANA = "ana@ejemplo.com";
    private static final String CORREO_CARLOS = "carlos@ejemplo.com";

    private static final String HASH_PRUEBA = "a".repeat(64);

    private static final String NOMBRE_ANA = "Ana";
    private static final String NOMBRE_CARLOS = "Carlos";

    @Autowired
    private VerificacionCorreoRepository verificacionCorreoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario ana;
    private Usuario carlos;
    private OffsetDateTime fechaBase;

    @BeforeEach
    void configurar() {

        Rol rol = new Rol(
                UUID.randomUUID(),
                ROL_HUESPED,
                "Rol de pruebas"
        );

        entityManager.persist(rol);

        ana = crearUsuario(
                rol,
                NOMBRE_ANA,
                CORREO_ANA,
                "3001234567",
                (byte) 1
        );

        carlos = crearUsuario(
                rol,
                NOMBRE_CARLOS,
                CORREO_CARLOS,
                "3007654321",
                (byte) 2
        );

        entityManager.persist(ana);
        entityManager.persist(carlos);
        entityManager.flush();

        fechaBase = OffsetDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS);
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private Usuario crearUsuario(
            Rol rol,
            String nombre,
            String correo,
            String telefono,
            byte identificador) {

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setRol(rol);
        usuario.setNombre(nombre);
        usuario.setApellido("Perez");
        usuario.setCorreo(correo);
        usuario.setTipoDocumento("CEDULA");

        usuario.setDocumentoCifrado(new byte[16]);
        usuario.setDocumentoNonce(new byte[12]);

        byte[] hash = new byte[32];
        hash[0] = identificador;

        usuario.setDocumentoHash(hash);
        usuario.setTelefono(telefono);
        usuario.setContrasenaHash("hash-de-prueba");

        usuario.setCorreoVerificado(false);
        usuario.setEstado(EstadoUsuario.ACTIVA);
        usuario.setRestringido(false);
        usuario.setIntentosFallidos(0);

        OffsetDateTime ahora = OffsetDateTime.now();

        usuario.setFechaRegistro(ahora);
        usuario.setFechaActualizacion(ahora);

        return usuario;
    }

    private VerificacionCorreo crearVerificacion(
            Usuario usuario,
            boolean verificado,
            OffsetDateTime fechaExpiracion) {

        VerificacionCorreo verificacion = new VerificacionCorreo();

        verificacion.setUsuario(usuario);
        verificacion.setCodigoHash(HASH_PRUEBA);
        verificacion.setFechaExpiracion(fechaExpiracion);
        verificacion.setVerificado(verificado);
        verificacion.setIntentos(0);

        return entityManager.persistAndFlush(verificacion);
    }

    private VerificacionCorreo buscarPorId(UUID id) {

        entityManager.clear();

        return entityManager.find(VerificacionCorreo.class, id);
    }

    // =========================
    // CONSULTA DE VERIFICACIONES
    // =========================

    @Test
    @DisplayName("Debe encontrar una verificación pendiente del usuario")
    void encontrarVerificacionPendiente() {

        VerificacionCorreo pendiente = crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(10)
        );

        entityManager.clear();

        var resultado = verificacionCorreoRepository
                .findByUsuarioAndVerificadoFalse(ana);

        assertTrue(resultado.isPresent());

        assertEquals(
                pendiente.getIdVerificacion(),
                resultado.orElseThrow().getIdVerificacion()
        );
    }

    @Test
    @DisplayName("No debe encontrar verificaciones que ya fueron completadas")
    void ignorarVerificacionesVerificadas() {

        crearVerificacion(
                ana,
                true,
                fechaBase.plusMinutes(10)
        );

        entityManager.clear();

        var resultado = verificacionCorreoRepository
                .findByUsuarioAndVerificadoFalse(ana);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("No debe encontrar verificaciones pertenecientes a otro usuario")
    void ignorarVerificacionesDeOtroUsuario() {

        crearVerificacion(
                carlos,
                false,
                fechaBase.plusMinutes(10)
        );

        entityManager.clear();

        var resultado = verificacionCorreoRepository
                .findByUsuarioAndVerificadoFalse(ana);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe generar una excepción cuando existen varias verificaciones pendientes")
    void rechazarMultiplesVerificacionesPendientes() {

        crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(5)
        );

        crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(10)
        );

        entityManager.clear();

        assertThrows(
                IncorrectResultSizeDataAccessException.class,
                () -> verificacionCorreoRepository
                        .findByUsuarioAndVerificadoFalse(ana)
        );
    }

    // =========================
    // CONSULTA DE LA ÚLTIMA VERIFICACIÓN
    // =========================

    @Test
    @DisplayName("Debe retornar la verificación pendiente con mayor fecha de expiración")
    void encontrarVerificacionMasReciente() {

        crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(5)
        );

        VerificacionCorreo masReciente = crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(30)
        );

        crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(10)
        );

        entityManager.clear();

        var resultado = verificacionCorreoRepository
                .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(ana);

        assertTrue(resultado.isPresent());

        assertEquals(
                masReciente.getIdVerificacion(),
                resultado.orElseThrow().getIdVerificacion()
        );
    }

    @Test
    @DisplayName("Debe ignorar verificaciones completadas al buscar la más reciente")
    void ignorarVerificadasAlBuscarLaMasReciente() {

        VerificacionCorreo pendiente = crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(5)
        );

        crearVerificacion(
                ana,
                true,
                fechaBase.plusMinutes(60)
        );

        entityManager.clear();

        var resultado = verificacionCorreoRepository
                .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(ana);

        assertTrue(resultado.isPresent());

        assertEquals(
                pendiente.getIdVerificacion(),
                resultado.orElseThrow().getIdVerificacion()
        );
    }

    @Test
    @DisplayName("No debe retornar verificaciones de otros usuarios")
    void ignorarOtrosUsuariosAlBuscarLaMasReciente() {

        crearVerificacion(
                carlos,
                false,
                fechaBase.plusMinutes(60)
        );

        entityManager.clear();

        var resultado = verificacionCorreoRepository
                .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(ana);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar un resultado vacío cuando no existen verificaciones")
    void retornarVacioSinVerificaciones() {

        var resultado = verificacionCorreoRepository
                .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(ana);

        assertTrue(resultado.isEmpty());
    }

    // =========================
    // INVALIDACIÓN DE CÓDIGOS
    // =========================

    @Test
    @DisplayName("Debe actualizar la fecha de expiración de todas las verificaciones pendientes")
    void invalidarTodasLasVerificacionesPendientes() {

        VerificacionCorreo primera = crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(5)
        );

        VerificacionCorreo segunda = crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(10)
        );

        OffsetDateTime nuevaFecha = fechaBase.minusMinutes(1);

        int actualizados = verificacionCorreoRepository
                .invalidarCodigosPendientes(ana, nuevaFecha);

        VerificacionCorreo primeraActualizada = buscarPorId(
                primera.getIdVerificacion()
        );

        VerificacionCorreo segundaActualizada = entityManager.find(
                VerificacionCorreo.class,
                segunda.getIdVerificacion()
        );

        assertAll(
                () -> assertEquals(2, actualizados),

                () -> assertTrue(
                        primeraActualizada.getFechaExpiracion()
                                .isEqual(nuevaFecha)
                ),

                () -> assertTrue(
                        segundaActualizada.getFechaExpiracion()
                                .isEqual(nuevaFecha)
                )
        );
    }

    @Test
    @DisplayName("No debe modificar las verificaciones que ya fueron completadas")
    void noModificarVerificacionesCompletadas() {

        OffsetDateTime expiracionOriginal = fechaBase.plusMinutes(10);

        VerificacionCorreo verificada = crearVerificacion(
                ana,
                true,
                expiracionOriginal
        );

        int actualizados = verificacionCorreoRepository
                .invalidarCodigosPendientes(
                        ana,
                        fechaBase.minusMinutes(1)
                );

        VerificacionCorreo resultado = buscarPorId(
                verificada.getIdVerificacion()
        );

        assertAll(
                () -> assertEquals(0, actualizados),

                () -> assertTrue(
                        resultado.getFechaExpiracion()
                                .isEqual(expiracionOriginal)
                ),

                () -> assertTrue(resultado.isVerificado())
        );
    }

    @Test
    @DisplayName("No debe modificar verificaciones pertenecientes a otros usuarios")
    void noModificarVerificacionesDeOtrosUsuarios() {

        OffsetDateTime expiracionOriginal = fechaBase.plusMinutes(10);

        VerificacionCorreo deCarlos = crearVerificacion(
                carlos,
                false,
                expiracionOriginal
        );

        crearVerificacion(
                ana,
                false,
                fechaBase.plusMinutes(5)
        );

        int actualizados = verificacionCorreoRepository
                .invalidarCodigosPendientes(
                        ana,
                        fechaBase.minusMinutes(1)
                );

        VerificacionCorreo resultado = buscarPorId(
                deCarlos.getIdVerificacion()
        );

        assertAll(
                () -> assertEquals(1, actualizados),

                () -> assertTrue(
                        resultado.getFechaExpiracion()
                                .isEqual(expiracionOriginal)
                )
        );
    }

    @Test
    @DisplayName("Debe retornar cero cuando no existen códigos pendientes para invalidar")
    void retornarCeroSinCodigosPendientes() {

        int actualizados = verificacionCorreoRepository
                .invalidarCodigosPendientes(
                        ana,
                        fechaBase.minusMinutes(1)
                );

        assertEquals(0, actualizados);
    }
}