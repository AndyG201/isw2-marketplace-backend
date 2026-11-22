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

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@ExtendWith(MockitoExtension.class)
class PublicacionPoliticaCancelacionServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private AlojamientoPublicacionQuery alojamientoQuery;
    @Mock private PoliticaCancelacionQuery politicaQuery;
    @InjectMocks private PublicacionPoliticaCancelacionService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();

    private Publicacion publicacionExistente() {
        Publicacion publicacion = mock(Publicacion.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));
        return publicacion;
    }

    private void propietarioValido() {
        AlojamientoPublicacionInfo alojamiento = mock(AlojamientoPublicacionInfo.class);
        when(alojamiento.idAnfitrion()).thenReturn(idAnfitrion);
        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento)).thenReturn(Optional.of(alojamiento));
    }

    @Test
    void rechazaIdentificadoresNulos() {
        assertThrows(AccesoNoPermitidoException.class,
                () -> service.seleccionarPolitica(null, idPublicacion, idPolitica));
        assertThrows(DatosInvalidosException.class,
                () -> service.seleccionarPolitica(idAnfitrion, null, idPolitica));
    }

    @Test
    void rechazaPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.consultarPolitica(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaSiNoSeEncuentraElAlojamiento() {
        publicacionExistente();
        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.consultarPolitica(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaAlQueNoEsElPropietario() {
        publicacionExistente();
        AlojamientoPublicacionInfo alojamiento = mock(AlojamientoPublicacionInfo.class);
        when(alojamiento.idAnfitrion()).thenReturn(UUID.randomUUID());
        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento)).thenReturn(Optional.of(alojamiento));

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarPolitica(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaPoliticaNoSeleccionable() {
        publicacionExistente();
        propietarioValido();
        when(politicaQuery.consultarPoliticaSeleccionable(idPolitica)).thenReturn(Optional.empty());

        assertThrows(DatosInvalidosException.class,
                () -> service.seleccionarPolitica(idAnfitrion, idPublicacion, idPolitica));
    }

    @Test
    void rechazaSiLaPoliticaAsociadaNoExiste() {
        Publicacion publicacion = publicacionExistente();
        propietarioValido();
        when(publicacion.getIdPolitica()).thenReturn(idPolitica);
        when(politicaQuery.consultarPorId(idPolitica)).thenReturn(Optional.empty());

        assertThrows(ConflictoNegocioException.class,
                () -> service.consultarPolitica(idAnfitrion, idPublicacion));
    }
}
