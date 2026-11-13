package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import co.edu.unbosque.marketplace.usuario.api.request.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import co.edu.unbosque.marketplace.usuario.contracts.CuentaRestringidaEvent;
@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private AdministradorService service;

    private final UUID idUsuario = UUID.randomUUID();

    private static RestringirUsuarioRequest solicitud(String motivo) {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();
        request.setMotivo(motivo);
        return request;
    }

    private void verificarFechaActualizada(
            OffsetDateTime fecha,
            OffsetDateTime antes,
            OffsetDateTime despues) {

        assertNotNull(fecha);
        assertFalse(fecha.isBefore(antes));
        assertFalse(fecha.isAfter(despues));
    }

    // REACTIVAR BLOQUEO

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void reactivarBloqueoRechazaCorreoInvalido(String correo) {
        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.reactivarBloqueo(correo)
        );

        assertEquals(
            "Debes proporcionar el correo del usuario.",
            error.getMessage()
        );

        verifyNoInteractions(usuarioRepository, jwtUtil);
    }

    @Test
    void reactivarBloqueoRechazaUsuarioInexistente() {
        when(usuarioRepository.findByCorreoIgnoreCase("ana@ejemplo.com"))
            .thenReturn(Optional.empty());

        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.reactivarBloqueo("ana@ejemplo.com")
        );

        assertEquals("Usuario no encontrado.", error.getMessage());
        verify(usuarioRepository).findByCorreoIgnoreCase("ana@ejemplo.com");
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(jwtUtil);
    }

    static Stream<Arguments> bloqueosNoReactivables() {
        return Stream.of(
            Arguments.of(4, "Bloqueo automático por intentos fallidos"),
            Arguments.of(5, "Otro motivo"),
            Arguments.of(5, (String) null)
        );
    }

    @ParameterizedTest
    @MethodSource("bloqueosNoReactivables")
    void reactivarBloqueoRechazaCuentaSinBloqueoAutomatico(
            int intentos,
            String motivo) {

        Usuario usuario = new Usuario();
        usuario.setIntentosFallidos(intentos);
        usuario.setMotivoBloqueo(motivo);

        when(usuarioRepository.findByCorreoIgnoreCase("ana@ejemplo.com"))
            .thenReturn(Optional.of(usuario));

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.reactivarBloqueo("ana@ejemplo.com")
        );

        assertEquals(
            "La cuenta no tiene un bloqueo automático pendiente de reactivación.",
            error.getMessage()
        );

        assertEquals(intentos, usuario.getIntentosFallidos());
        assertEquals(motivo, usuario.getMotivoBloqueo());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    void reactivarBloqueoReiniciaIntentosYLimpiaMotivo(int intentos) {
        Usuario usuario = new Usuario();
        usuario.setIntentosFallidos(intentos);
        usuario.setMotivoBloqueo("Bloqueo automático por intentos fallidos");
        usuario.setFechaActualizacion(
            OffsetDateTime.parse("2020-01-01T00:00:00Z")
        );

        when(usuarioRepository.findByCorreoIgnoreCase("ana@ejemplo.com"))
            .thenReturn(Optional.of(usuario));

        OffsetDateTime antes = OffsetDateTime.now();

        service.reactivarBloqueo("  ana@ejemplo.com  ");

        OffsetDateTime despues = OffsetDateTime.now();

        assertAll(
            () -> assertEquals(0, usuario.getIntentosFallidos()),
            () -> assertNull(usuario.getMotivoBloqueo()),
            () -> verificarFechaActualizada(
                usuario.getFechaActualizacion(), antes, despues
            )
        );

        verify(usuarioRepository).findByCorreoIgnoreCase("ana@ejemplo.com");
        verify(usuarioRepository).save(same(usuario));
        verifyNoInteractions(jwtUtil);
    }

    // RESTRINGIR USUARIO

    static Stream<Arguments> restriccionesInvalidas() {
        UUID id = UUID.randomUUID();

        return Stream.of(
            Arguments.of((UUID) null, solicitud("Motivo válido")),
            Arguments.of(id, (RestringirUsuarioRequest) null),
            Arguments.of(id, solicitud(null)),
            Arguments.of(id, solicitud("")),
            Arguments.of(id, solicitud("   ")),
            Arguments.of(id, solicitud("a".repeat(256)))
        );
    }

    @ParameterizedTest
    @MethodSource("restriccionesInvalidas")
    void restringirUsuarioRechazaDatosInvalidos(
            UUID id,
            RestringirUsuarioRequest request) {

        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.restringirUsuario(id, request)
        );

        assertEquals(
            "Debes proporcionar un motivo válido para la restricción.",
            error.getMessage()
        );

        verifyNoInteractions(usuarioRepository, jwtUtil);
    }

    @Test
    void restringirUsuarioRechazaUsuarioInexistente() {
        when(usuarioRepository.findById(idUsuario))
            .thenReturn(Optional.empty());

        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.restringirUsuario(
                idUsuario, solicitud("Incumplimiento")
            )
        );

        assertEquals("Usuario no encontrado.", error.getMessage());
        verify(usuarioRepository).findById(idUsuario);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void restringirUsuarioRechazaCuentaYaRestringida() {
        Usuario usuario = new Usuario();
        usuario.setRestringido(true);
        usuario.setMotivoRestriccion("Motivo anterior");

        when(usuarioRepository.findById(idUsuario))
            .thenReturn(Optional.of(usuario));

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.restringirUsuario(
                idUsuario, solicitud("Motivo nuevo")
            )
        );

        assertEquals(
            "La cuenta ya tiene una restricción administrativa.",
            error.getMessage()
        );
        assertEquals("Motivo anterior", usuario.getMotivoRestriccion());

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 255})
    void restringirUsuarioGuardaMotivoRecortadoEInvalidaSesiones(
            int longitud) {

        Usuario usuario = new Usuario();
        usuario.setFechaActualizacion(
            OffsetDateTime.parse("2020-01-01T00:00:00Z")
        );
        String motivo = "a".repeat(longitud);

        when(usuarioRepository.findById(idUsuario))
            .thenReturn(Optional.of(usuario));

        OffsetDateTime antes = OffsetDateTime.now();

        service.restringirUsuario(
            idUsuario, solicitud("  " + motivo + "  ")
        );

        OffsetDateTime despues = OffsetDateTime.now();

        assertAll(
            () -> assertTrue(usuario.isRestringido()),
            () -> assertEquals(motivo, usuario.getMotivoRestriccion()),
            () -> verificarFechaActualizada(
                usuario.getFechaActualizacion(), antes, despues
            )
        );

        verify(usuarioRepository).save(same(usuario));
        verify(jwtUtil).invalidarSesiones(idUsuario);
        verify(eventPublisher).publishEvent(
                any(CuentaRestringidaEvent.class)
        );
    }

    // LEVANTAR RESTRICCIÓN

    @Test
    void levantarRestriccionRechazaIdNulo() {
        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.levantarRestriccion(null)
        );

        assertEquals(
            "Debes proporcionar el identificador del usuario.",
            error.getMessage()
        );

        verifyNoInteractions(usuarioRepository, jwtUtil);
    }

    @Test
    void levantarRestriccionRechazaUsuarioInexistente() {
        when(usuarioRepository.findById(idUsuario))
            .thenReturn(Optional.empty());

        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.levantarRestriccion(idUsuario)
        );

        assertEquals("Usuario no encontrado.", error.getMessage());
        verify(usuarioRepository).findById(idUsuario);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void levantarRestriccionRechazaCuentaSinRestriccion() {
        Usuario usuario = new Usuario();
        usuario.setRestringido(false);

        when(usuarioRepository.findById(idUsuario))
            .thenReturn(Optional.of(usuario));

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.levantarRestriccion(idUsuario)
        );

        assertEquals(
            "La cuenta no tiene una restricción administrativa vigente.",
            error.getMessage()
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void levantarRestriccionLimpiaMotivoYActualizaUsuario() {
        Usuario usuario = new Usuario();
        usuario.setRestringido(true);
        usuario.setMotivoRestriccion("Incumplimiento");
        usuario.setFechaActualizacion(
            OffsetDateTime.parse("2020-01-01T00:00:00Z")
        );

        when(usuarioRepository.findById(idUsuario))
            .thenReturn(Optional.of(usuario));

        OffsetDateTime antes = OffsetDateTime.now();

        service.levantarRestriccion(idUsuario);

        OffsetDateTime despues = OffsetDateTime.now();

        assertAll(
            () -> assertFalse(usuario.isRestringido()),
            () -> assertNull(usuario.getMotivoRestriccion()),
            () -> verificarFechaActualizada(
                usuario.getFechaActualizacion(), antes, despues
            )
        );

        verify(usuarioRepository).save(same(usuario));
        verifyNoInteractions(jwtUtil);
    }

    // CONSULTAR USUARIOS

    @Test
    void consultarUsuariosRetornaListaVaciaSiNoHayUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        var resultado = service.consultarUsuarios();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findAll();
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void consultarUsuariosMapeaTodosLosCamposDelResponse() {
        // Permite configurar el rol sin asumir el nombre de su clase.
        Usuario usuario = mock(Usuario.class, RETURNS_DEEP_STUBS);
        EstadoUsuario estado = EstadoUsuario.values()[0];

        when(usuario.getIdUsuario()).thenReturn(idUsuario);
        when(usuario.getNombre()).thenReturn("Ana");
        when(usuario.getApellido()).thenReturn("Perez");
        when(usuario.getCorreo()).thenReturn("ana@ejemplo.com");
        when(usuario.getRol().getNombre()).thenReturn("HUESPED");
        when(usuario.getEstado()).thenReturn(estado);
        when(usuario.isRestringido()).thenReturn(true);
        when(usuario.getMotivoRestriccion()).thenReturn("Incumplimiento");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        var resultado = service.consultarUsuarios();

        assertEquals(1, resultado.size());
        var response = resultado.get(0);

        assertAll(
            () -> assertEquals(idUsuario, response.getIdUsuario()),
            () -> assertEquals("Ana", response.getNombre()),
            () -> assertEquals("Perez", response.getApellido()),
            () -> assertEquals("ana@ejemplo.com", response.getCorreo()),
            () -> assertEquals("HUESPED", response.getRol()),
            () -> assertEquals(estado.name(), response.getEstado()),
            () -> assertTrue(response.isRestringido()),
            () -> assertEquals(
                "Incumplimiento", response.getMotivoRestriccion()
            )
        );

        verify(usuarioRepository).findAll();
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void consultarUsuariosPermiteRolYEstadoNulos() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setRol(null);
        usuario.setEstado(null);
        usuario.setRestringido(false);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        var resultado = service.consultarUsuarios();

        assertEquals(1, resultado.size());
        var response = resultado.get(0);

        assertAll(
            () -> assertEquals(idUsuario, response.getIdUsuario()),
            () -> assertNull(response.getRol()),
            () -> assertNull(response.getEstado()),
            () -> assertFalse(response.isRestringido()),
            () -> assertNull(response.getMotivoRestriccion())
        );

        verify(usuarioRepository).findAll();
        verifyNoInteractions(jwtUtil);
    }
}