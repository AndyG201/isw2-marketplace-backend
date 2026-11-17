package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.RevisionPublicacionRepository;
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

    @Mock
    private Publicacion publicacion;

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
                AccessDeniedException.class,
                () -> service.aprobarPublicacion(null, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConAdministradorNulo() {

        assertThrows(
                AccessDeniedException.class,
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
                AccessDeniedException.class,
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
                AccessDeniedException.class,
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
                IllegalArgumentException.class,
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
                IllegalArgumentException.class,
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
                java.util.NoSuchElementException.class,
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
                java.util.NoSuchElementException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalStateException.class,
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
                IllegalArgumentException.class,
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
                IllegalArgumentException.class,
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
                IllegalArgumentException.class,
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
}