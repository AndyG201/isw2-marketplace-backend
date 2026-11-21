package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaQuery;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioReservaQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionReservasAfectadasServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private RevisionPublicacionRepository revisionRepository;
    @Mock private CotizacionReservaQuery cotizacionQuery;
    @Mock private ReservaAfectadaQuery reservaQuery;
    @Mock private UsuarioReservaQuery usuarioQuery;
    @InjectMocks private PublicacionReservasAfectadasService service;

    private final UUID idPublicacion = UUID.randomUUID();

    @Test
    void rechazaIdentificadorNulo() {
        assertThrows(DatosInvalidosException.class, () -> service.consultarReservasAfectadas(null));
    }

    @Test
    void rechazaPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.consultarReservasAfectadas(idPublicacion));
    }

    @Test
    void rechazaPublicacionQueNoEstaBloqueada() {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getEstado()).thenReturn("ACTIVA");
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));

        assertThrows(ConflictoNegocioException.class, () -> service.consultarReservasAfectadas(idPublicacion));
    }
}
