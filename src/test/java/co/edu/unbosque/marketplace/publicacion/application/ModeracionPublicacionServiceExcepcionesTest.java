package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@ExtendWith(MockitoExtension.class)
class ModeracionPublicacionServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private RevisionPublicacionRepository revisionRepository;
    @Mock private AlojamientoPublicacionQuery alojamientoQuery;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ModeracionPublicacionService service;

    private final UUID admin = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();

    private Publicacion publicacionEnEstado(String estado) {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getEstado()).thenReturn(estado);
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));
        return publicacion;
    }

    @Test
    void rechazaAdministradorNulo() {
        assertThrows(DatosInvalidosException.class, () -> service.bloquearPublicacion(null, idPublicacion, "motivo"));
    }

    @Test
    void rechazaMotivosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> service.bloquearPublicacion(admin, idPublicacion, null));
        assertThrows(DatosInvalidosException.class, () -> service.bloquearPublicacion(admin, idPublicacion, "   "));
        assertThrows(DatosInvalidosException.class,
                () -> service.desbloquearPublicacion(admin, idPublicacion, "x".repeat(2001)));
    }

    @Test
    void rechazaPublicacionNulaOInexistente() {
        assertThrows(DatosInvalidosException.class, () -> service.bloquearPublicacion(admin, null, "motivo"));

        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.bloquearPublicacion(admin, idPublicacion, "motivo"));
    }

    @Test
    void noBloqueaUnaPublicacionYaBloqueada() {
        publicacionEnEstado("BLOQUEADA");

        assertThrows(ConflictoNegocioException.class,
                () -> service.bloquearPublicacion(admin, idPublicacion, "motivo"));
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void soloDesbloqueaPublicacionesBloqueadas() {
        publicacionEnEstado("ACTIVA");

        assertThrows(ConflictoNegocioException.class,
                () -> service.desbloquearPublicacion(admin, idPublicacion, "motivo"));
    }

    @Test
    void noDesbloqueaSiNoSeEncuentraAlAnfitrion() {
        Publicacion publicacion = publicacionEnEstado("BLOQUEADA");
        UUID idAlojamiento = UUID.randomUUID();
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento)).thenReturn(Optional.empty());

        assertThrows(ConflictoNegocioException.class,
                () -> service.desbloquearPublicacion(admin, idPublicacion, "motivo"));
        verify(publicacionRepository, never()).save(any());
    }
}
