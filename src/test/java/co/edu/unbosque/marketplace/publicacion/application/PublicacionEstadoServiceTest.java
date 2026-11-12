package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

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
import co.edu.unbosque.marketplace.publicacion.api.response.EstadoPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionEstadoServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoPublicacionQuery alojamientoPublicacionQuery;

    @Mock
    private UsuarioPublicacionQuery usuarioPublicacionQuery;

    @Mock
    private Publicacion publicacion;

    @Mock
    private UsuarioPublicacionInfo anfitrion;

    @InjectMocks
    private PublicacionEstadoService service;

    private UUID idAnfitrion;
    private UUID idPublicacion;
    private UUID idAlojamiento;

    @BeforeEach
    void setUp() {

        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();

        when(anfitrion.esAnfitrion()).thenReturn(true);
        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(true);
    }

    private void configurarAnfitrionValido() {

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));
    }

    private void prepararPublicacion(String estado) {

        configurarAnfitrionValido();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(publicacion.getIdPublicacion())
                .thenReturn(idPublicacion);

        when(publicacion.getIdAlojamiento())
                .thenReturn(idAlojamiento);

        when(publicacion.getEstado())
                .thenReturn(estado);

        when(alojamientoPublicacionQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));
    }

    @Test
    void pausarPublicacionCorrectamente() {

        prepararPublicacion("ACTIVA");

        EstadoPublicacionResponse respuesta =
                service.pausarPublicacion(idAnfitrion, idPublicacion);

        assertNotNull(respuesta);

        verify(publicacion).setEstado("PAUSADA");

        verify(publicacion).setFechaActualizacion(
                any(java.time.OffsetDateTime.class));

        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void reactivarPublicacionCorrectamente() {

        prepararPublicacion("PAUSADA");

        EstadoPublicacionResponse respuesta =
                service.reactivarPublicacion(idAnfitrion, idPublicacion);

        assertNotNull(respuesta);

        verify(publicacion).setEstado("ACTIVA");

        verify(publicacion).setFechaActualizacion(
                any(java.time.OffsetDateTime.class));

        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void pausarConAnfitrionNulo() {

        assertThrows(
                AccessDeniedException.class,
                () -> service.pausarPublicacion(null, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery,
                usuarioPublicacionQuery
        );
    }

    @Test
    void reactivarConAnfitrionNulo() {

        assertThrows(
                AccessDeniedException.class,
                () -> service.reactivarPublicacion(null, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery,
                usuarioPublicacionQuery
        );
    }

    @Test
    void pausarConAnfitrionInexistente() {

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void reactivarConAnfitrionInexistente() {

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void pausarConRolDiferenteDeAnfitrion() {

        configurarAnfitrionValido();

        when(anfitrion.esAnfitrion()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void reactivarConRolDiferenteDeAnfitrion() {

        configurarAnfitrionValido();

        when(anfitrion.esAnfitrion()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void pausarConCuentaNoHabilitada() {

        configurarAnfitrionValido();

        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void reactivarConCuentaNoHabilitada() {

        configurarAnfitrionValido();

        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void pausarConIdentificadorPublicacionNulo() {

        configurarAnfitrionValido();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pausarPublicacion(idAnfitrion, null)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void reactivarConIdentificadorPublicacionNulo() {

        configurarAnfitrionValido();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.reactivarPublicacion(idAnfitrion, null)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void reactivarPublicacionInexistente() {

        configurarAnfitrionValido();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(alojamientoPublicacionQuery);
    }

    @Test
    void pausarSinAlojamientoAsociado() {

        configurarAnfitrionValido();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(publicacion.getIdAlojamiento())
                .thenReturn(idAlojamiento);

        when(alojamientoPublicacionQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarPublicacionDeOtroAnfitrion() {

        configurarAnfitrionValido();

        UUID otroAnfitrion = UUID.randomUUID();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(publicacion.getIdAlojamiento())
                .thenReturn(idAlojamiento);

        when(alojamientoPublicacionQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(otroAnfitrion));

        assertThrows(
                AccessDeniedException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reactivarPublicacionDeOtroAnfitrion() {

        configurarAnfitrionValido();

        UUID otroAnfitrion = UUID.randomUUID();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(publicacion.getIdAlojamiento())
                .thenReturn(idAlojamiento);

        when(alojamientoPublicacionQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(otroAnfitrion));

        assertThrows(
                AccessDeniedException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarPublicacionBloqueada() {

        prepararPublicacion("BLOQUEADA");

        assertThrows(
                IllegalStateException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reactivarPublicacionBloqueada() {

        prepararPublicacion("BLOQUEADA");

        assertThrows(
                IllegalStateException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarPublicacionQueNoEstaActiva() {

        prepararPublicacion("PAUSADA");

        assertThrows(
                IllegalStateException.class,
                () -> service.pausarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reactivarPublicacionQueNoEstaPausada() {

        prepararPublicacion("ACTIVA");

        assertThrows(
                IllegalStateException.class,
                () -> service.reactivarPublicacion(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarRetornaRespuesta() {

        prepararPublicacion("ACTIVA");

        EstadoPublicacionResponse respuesta =
                service.pausarPublicacion(idAnfitrion, idPublicacion);

        assertNotNull(respuesta);
    }

    @Test
    void reactivarRetornaRespuesta() {

        prepararPublicacion("PAUSADA");

        EstadoPublicacionResponse respuesta =
                service.reactivarPublicacion(idAnfitrion, idPublicacion);

        assertNotNull(respuesta);
    }
}
