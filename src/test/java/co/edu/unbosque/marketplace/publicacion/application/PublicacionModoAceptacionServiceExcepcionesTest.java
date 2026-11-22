package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.dto.ConfigurarModoAceptacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionModoAceptacionServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private AlojamientoPublicacionQuery alojamientoQuery;
    @Mock private UsuarioAdministracionQuery usuarioAdministracionQuery;
    @Mock private UsuarioPublicacionQuery usuarioPublicacionQuery;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private PublicacionModoAceptacionService service;

    private final UUID idActor = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();

    private Publicacion publicacion(String modo) {
        Publicacion publicacion = mock(Publicacion.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        lenient().when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        lenient().when(publicacion.getModoAceptacion()).thenReturn(modo);
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));
        return publicacion;
    }

    private ConfigurarModoAceptacionRequest solicitud(String modo) {
        ConfigurarModoAceptacionRequest solicitud = new ConfigurarModoAceptacionRequest();
        solicitud.setModoAceptacion(modo);
        return solicitud;
    }

    @Test
    void rechazaIdentificadoresNulos() {
        assertThrows(AccesoNoPermitidoException.class,
                () -> service.actualizarModoAceptacion(null, idPublicacion, solicitud("INMEDIATA")));
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarModoAceptacion(idActor, null, solicitud("INMEDIATA")));
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarModoAceptacion(idActor, idPublicacion, null));
    }

    @Test
    void rechazaPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.consultarModoAceptacion(idActor, idPublicacion));
    }

    @Test
    void rechazaSiNoSeEncuentraAlAnfitrion() {
        publicacion("INMEDIATA");
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.consultarModoAceptacion(idActor, idPublicacion));
    }

    @Test
    void rechazaUsuarioSinPermisosParaLaPublicacion() {
        publicacion("INMEDIATA");
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento)).thenReturn(Optional.of(idActor));
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(false);
        when(usuarioPublicacionQuery.consultarParaPublicacion(idActor)).thenReturn(Optional.empty());

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarModoAceptacion(idActor, idPublicacion));
    }

    @Test
    void rechazaAUnUsuarioQueNoEsAnfitrionNiPropietario() {
        publicacion("INMEDIATA");
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento)).thenReturn(Optional.of(UUID.randomUUID()));
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(false);
        when(usuarioPublicacionQuery.consultarParaPublicacion(idActor))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(true, true, true, false, true, 0)));

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarModoAceptacion(idActor, idPublicacion));
    }

    @Test
    void consultarFallaSiLaPublicacionNoTieneModoConfigurado() {
        publicacion(null);
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento)).thenReturn(Optional.of(idActor));
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(true);

        assertThrows(ConflictoNegocioException.class,
                () -> service.consultarModoAceptacion(idActor, idPublicacion));
    }

    @Test
    void consultarFallaSiElModoGuardadoEsInvalido() {
        publicacion("MODO_RARO");
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento)).thenReturn(Optional.of(idActor));
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(true);

        assertThrows(ConflictoNegocioException.class,
                () -> service.consultarModoAceptacion(idActor, idPublicacion));
    }
}
