package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RevisionAdministrativaServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoPublicacionQuery alojamientoQuery;

    @Mock
    private UsuarioAdministracionQuery usuarioAdministracionQuery;

    @Mock
    private UsuarioPublicacionQuery usuarioPublicacionQuery;

    @Mock
    private RevisionPublicacionRepository revisionRepository;

    @Mock(answer = org.mockito.Answers.CALLS_REAL_METHODS)
    private Publicacion publicacion;

    @Mock
    private CambiosPublicacionService cambios;

    @InjectMocks
    private RevisionAdministrativaService service;

    private UUID idAdministrador;
    private UUID idAnfitrion;
    private UUID idPublicacion;
    private UUID idAlojamiento;

    private AtomicReference<String> estadoPublicacion;

    @BeforeEach
    void setUp() {

        idAdministrador = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();

        estadoPublicacion = new AtomicReference<>("PENDIENTE_REVISION");

        when(usuarioAdministracionQuery.esAdministrador(idAdministrador))
                .thenReturn(true);

        when(publicacion.getIdPublicacion())
                .thenReturn(idPublicacion);

        when(publicacion.getIdAlojamiento())
                .thenReturn(idAlojamiento);

        when(publicacion.getEstado())
                .thenAnswer(invocation -> estadoPublicacion.get());

        doAnswer(invocation -> {
            estadoPublicacion.set(invocation.getArgument(0));
            return null;
        }).when(publicacion).setEstado(any(String.class));

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(revisionRepository.save(any(RevisionPublicacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepararAnfitrionValido() {

        UsuarioPublicacionInfo anfitrion = new UsuarioPublicacionInfo(
                true,
                true,
                true,
                false,
                true,
                0
        );

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));
    }

    private void prepararPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());
    }

    private void prepararAnfitrion(UsuarioPublicacionInfo anfitrion) {

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));
    }

    @Test
    void aprobarPublicacionCorrectamente() {

        prepararAnfitrionValido();

        var respuesta = service.aprobarPublicacion(
                idAdministrador,
                idPublicacion
        );

        assertNotNull(respuesta);
        verify(publicacion).setEstado("ACTIVA");
        verify(publicacion).setFechaActualizacion(any(OffsetDateTime.class));
        verify(publicacionRepository).save(publicacion);
        verify(revisionRepository).save(any(RevisionPublicacion.class));
    }

    @Test
    void rechazarPublicacionCorrectamente() {

        var respuesta = service.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                "Información incompleta"
        );

        assertNotNull(respuesta);
        verify(publicacion).setEstado("BORRADOR");
        verify(publicacion).setFechaActualizacion(any(OffsetDateTime.class));
        verify(publicacionRepository).save(publicacion);
        verify(revisionRepository).save(any(RevisionPublicacion.class));

        verifyNoInteractions(alojamientoQuery, usuarioPublicacionQuery);
    }

    @Test
    void aprobarConAdministradorNulo() {

        assertThrows(
                AccesoNoPermitidoException.class,
                () -> service.aprobarPublicacion(null, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConAdministradorNulo() {

        assertThrows(
                AccesoNoPermitidoException.class,
                () -> service.rechazarPublicacion(
                        null,
                        idPublicacion,
                        "Motivo"
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarConUsuarioQueNoEsAdministrador() {

        when(usuarioAdministracionQuery.esAdministrador(idAdministrador))
                .thenReturn(false);

        assertThrows(
                AccesoNoPermitidoException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConUsuarioQueNoEsAdministrador() {

        when(usuarioAdministracionQuery.esAdministrador(idAdministrador))
                .thenReturn(false);

        assertThrows(
                AccesoNoPermitidoException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        "Motivo"
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarConIdentificadorDePublicacionNulo() {

        assertThrows(
                DatosInvalidosException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        null
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConIdentificadorDePublicacionNulo() {

        assertThrows(
                DatosInvalidosException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        null,
                        "Motivo"
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarPublicacionInexistente() {

        prepararPublicacionInexistente();

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verifyNoInteractions(alojamientoQuery, usuarioPublicacionQuery);
    }

    @Test
    void rechazarPublicacionInexistente() {

        prepararPublicacionInexistente();

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        "Motivo"
                )
        );

        verifyNoInteractions(alojamientoQuery, usuarioPublicacionQuery);
    }

    @Test
    void aprobarPublicacionEnEstadoIncorrecto() {

        estadoPublicacion.set("BORRADOR");

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verifyNoInteractions(alojamientoQuery, usuarioPublicacionQuery);
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void rechazarPublicacionEnEstadoIncorrecto() {

        estadoPublicacion.set("ACTIVA");

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        "Motivo"
                )
        );

        verify(publicacionRepository, never()).save(any());
        verifyNoInteractions(revisionRepository);
    }

    @Test
    void aprobarSinAlojamientoAsociado() {

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verifyNoInteractions(usuarioPublicacionQuery);
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarSinCuentaDeAnfitrion() {

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConAnfitrionInactivo() {

        prepararAnfitrion(new UsuarioPublicacionInfo(
                true, true, false, false, true, 0
        ));

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConAnfitrionRestringido() {

        prepararAnfitrion(new UsuarioPublicacionInfo(
                true, true, true, true, true, 0
        ));

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConCorreoDeAnfitrionNoVerificado() {

        prepararAnfitrion(new UsuarioPublicacionInfo(
                true, true, true, false, false, 0
        ));

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConCincoIntentosFallidosDelAnfitrion() {

        prepararAnfitrion(new UsuarioPublicacionInfo(
                true, true, true, false, true, 5
        ));

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConRolIncorrectoDelAnfitrion() {

        prepararAnfitrion(new UsuarioPublicacionInfo(
                false, true, true, false, true, 0
        ));

        assertThrows(
                ConflictoNegocioException.class,
                () -> service.aprobarPublicacion(
                        idAdministrador,
                        idPublicacion
                )
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void rechazarConMotivoNulo() {

        assertThrows(
                DatosInvalidosException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        null
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConMotivoVacio() {

        assertThrows(
                DatosInvalidosException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        "   "
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConMotivoMayorA2000Caracteres() {

        String motivo = "A".repeat(2001);

        assertThrows(
                DatosInvalidosException.class,
                () -> service.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        motivo
                )
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConMotivoDe2000Caracteres() {

        String motivo = "A".repeat(2000);

        var respuesta = service.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                motivo
        );

        assertNotNull(respuesta);
        verify(publicacion).setEstado("BORRADOR");
        verify(publicacionRepository).save(publicacion);
        verify(revisionRepository).save(any(RevisionPublicacion.class));
    }

    @Test
    void alAprobarLosCambiosDeUnaPublicacionPausadaVuelveAPausada() {
        prepararAnfitrionValido();
        when(cambios.resolver(eq(idPublicacion), any())).thenReturn(Optional.of("PAUSADA"));

        var respuesta = service.aprobarPublicacion(idAdministrador, idPublicacion);

        assertEquals("PAUSADA", estadoPublicacion.get());
        assertEquals("PAUSADA", respuesta.getEstadoActual());
        assertTrue(respuesta.getMensaje().contains("sigue pausada"));
    }

    @Test
    void alAprobarLosCambiosDeUnaPublicacionActivaQuedaActiva() {
        prepararAnfitrionValido();
        when(cambios.resolver(eq(idPublicacion), any())).thenReturn(Optional.of("ACTIVA"));

        service.aprobarPublicacion(idAdministrador, idPublicacion);

        assertEquals("ACTIVA", estadoPublicacion.get());
    }

    @Test
    void alRechazarCierraLaSolicitudDeCambiosYDevuelveABorrador() {
        service.rechazarPublicacion(idAdministrador, idPublicacion, "Las fotos no corresponden");

        verify(cambios).resolver(eq(idPublicacion), any());
        assertEquals("BORRADOR", estadoPublicacion.get());
    }
}
