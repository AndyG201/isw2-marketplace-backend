package co.edu.unbosque.marketplace.notificacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;

import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(
        properties = "spring.test.database.replace=none"
)
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID idUsuario;
    private UUID otroUsuario;

    @BeforeEach
    void configurar() {

        // Crear rol de prueba.
        Rol rol = new Rol(
                UUID.randomUUID(),
                "HUESPED",
                "Rol para pruebas de notificaciones"
        );

        entityManager.persist(rol);

        // Crear usuarios reales para respetar la llave foránea.
        Usuario usuarioUno = crearUsuario(
                rol,
                "Ana",
                "ana@test.com",
                "3001234567",
                (byte) 1
        );

        Usuario usuarioDos = crearUsuario(
                rol,
                "Carlos",
                "carlos@test.com",
                "3007654321",
                (byte) 2
        );

        entityManager.persist(usuarioUno);
        entityManager.persist(usuarioDos);

        entityManager.flush();

        idUsuario = usuarioUno.getIdUsuario();
        otroUsuario = usuarioDos.getIdUsuario();

        entityManager.clear();
    }

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
        usuario.setApellido("Pruebas");
        usuario.setCorreo(correo);
        usuario.setTipoDocumento("CEDULA");

        usuario.setDocumentoCifrado(new byte[16]);
        usuario.setDocumentoNonce(new byte[12]);

        byte[] hash = new byte[32];
        hash[0] = identificador;

        usuario.setDocumentoHash(hash);
        usuario.setTelefono(telefono);
        usuario.setContrasenaHash("hash-de-prueba");

        usuario.setCorreoVerificado(true);
        usuario.setEstado(EstadoUsuario.ACTIVA);
        usuario.setRestringido(false);
        usuario.setIntentosFallidos(0);

        OffsetDateTime ahora = OffsetDateTime.now();

        usuario.setFechaRegistro(ahora);
        usuario.setFechaActualizacion(ahora);

        return usuario;
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescDebeRetornarNotificacionesDelUsuario() {

        Notificacion primera = crearNotificacion(
                idUsuario,
                "PRIMERA",
                OffsetDateTime.parse("2026-11-19T10:00:00-05:00")
        );

        Notificacion segunda = crearNotificacion(
                idUsuario,
                "SEGUNDA",
                OffsetDateTime.parse("2026-11-20T10:00:00-05:00")
        );

        Notificacion tercera = crearNotificacion(
                otroUsuario,
                "OTRA_NOTIFICACION",
                OffsetDateTime.parse("2026-11-21T10:00:00-05:00")
        );

        notificacionRepository.saveAll(
                List.of(primera, segunda, tercera)
        );

        entityManager.flush();
        entityManager.clear();

        List<Notificacion> resultado =
                notificacionRepository
                        .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);

        assertEquals(2, resultado.size());

        assertTrue(resultado.stream().allMatch(
                notificacion ->
                        idUsuario.equals(notificacion.getIdUsuario())
        ));
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescDebeOrdenarPorFechaDescendente() {

        Notificacion antigua = crearNotificacion(
                idUsuario,
                "ANTIGUA",
                OffsetDateTime.parse("2026-11-18T10:00:00-05:00")
        );

        Notificacion reciente = crearNotificacion(
                idUsuario,
                "RECIENTE",
                OffsetDateTime.parse("2026-11-21T10:00:00-05:00")
        );

        Notificacion intermedia = crearNotificacion(
                idUsuario,
                "INTERMEDIA",
                OffsetDateTime.parse("2026-11-20T10:00:00-05:00")
        );

        notificacionRepository.saveAll(
                List.of(antigua, reciente, intermedia)
        );

        entityManager.flush();
        entityManager.clear();

        List<Notificacion> resultado =
                notificacionRepository
                        .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);

        assertEquals(3, resultado.size());

        assertAll(
                () -> assertEquals(
                        "RECIENTE",
                        resultado.get(0).getTipo()
                ),
                () -> assertEquals(
                        "INTERMEDIA",
                        resultado.get(1).getTipo()
                ),
                () -> assertEquals(
                        "ANTIGUA",
                        resultado.get(2).getTipo()
                )
        );
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescDebeRetornarListaVaciaCuandoNoHayResultados() {

        List<Notificacion> resultado =
                notificacionRepository
                        .findByIdUsuarioOrderByFechaCreacionDesc(
                                UUID.randomUUID()
                        );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescNoDebeMezclarUsuarios() {

        notificacionRepository.saveAll(
                List.of(
                        crearNotificacion(
                                idUsuario,
                                "USUARIO_UNO",
                                OffsetDateTime.now()
                        ),
                        crearNotificacion(
                                otroUsuario,
                                "USUARIO_DOS",
                                OffsetDateTime.now()
                        )
                )
        );

        entityManager.flush();
        entityManager.clear();

        List<Notificacion> resultado =
                notificacionRepository
                        .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);

        assertEquals(1, resultado.size());
        assertEquals(idUsuario, resultado.get(0).getIdUsuario());
        assertEquals("USUARIO_UNO", resultado.get(0).getTipo());
    }

    private Notificacion crearNotificacion(
            UUID idUsuario,
            String tipo,
            OffsetDateTime fechaCreacion) {

        Notificacion notificacion = new Notificacion();

        notificacion.setIdNotificacion(UUID.randomUUID());
        notificacion.setIdUsuario(idUsuario);
        notificacion.setIdReserva(null);
        notificacion.setIdPublicacion(null);
        notificacion.setTipo(tipo);
        notificacion.setMensaje("Mensaje de prueba: " + tipo);
        notificacion.setFechaCreacion(fechaCreacion);
        notificacion.setFechaLectura(null);

        return notificacion;
    }
}
