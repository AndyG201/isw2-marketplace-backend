package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.dto.ConfigurarConfiguracionComercialRequest;
import co.edu.unbosque.marketplace.publicacion.application.dto.ServicioAdicionalRequest;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalCommand;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionConfiguracionComercialServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private AlojamientoPublicacionQuery alojamientoQuery;
    @Mock private UsuarioPublicacionQuery usuarioQuery;
    @Mock private ServicioAdicionalCommand servicioAdicionalCommand;
    @Mock private ServicioAdicionalQuery servicioAdicionalQuery;
    @InjectMocks private PublicacionConfiguracionComercialService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();

    private static ServicioAdicionalRequest servicio(String nombre, String costo) {
        ServicioAdicionalRequest s = new ServicioAdicionalRequest();
        s.setNombre(nombre);
        s.setCosto(costo == null ? null : new BigDecimal(costo));
        return s;
    }

    private static ConfigurarConfiguracionComercialRequest solicitud(String tarifa, List<ServicioAdicionalRequest> servicios) {
        ConfigurarConfiguracionComercialRequest r = new ConfigurarConfiguracionComercialRequest();
        r.setTarifaLimpieza(tarifa == null ? null : new BigDecimal(tarifa));
        r.setServiciosAdicionales(servicios);
        return r;
    }

    private void actualizaRechaza(Class<? extends RuntimeException> tipo, ConfigurarConfiguracionComercialRequest solicitud) {
        assertThrows(tipo, () -> service.actualizarConfiguracion(idAnfitrion, idPublicacion, solicitud));
    }

    private void publicacionExistente() {
        Publicacion publicacion = mock(Publicacion.class);
        lenient().when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));
    }

    @Test
    void rechazaIdentificadoresNulos() {
        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarConfiguracion(null, idPublicacion));
        assertThrows(DatosInvalidosException.class,
                () -> service.consultarConfiguracion(idAnfitrion, null));
    }

    @Test
    void rechazaSolicitudesInvalidas() {
        actualizaRechaza(DatosInvalidosException.class, null);
        actualizaRechaza(DatosInvalidosException.class, solicitud(null, List.of()));
        actualizaRechaza(DatosInvalidosException.class, solicitud("-1", List.of()));
        actualizaRechaza(DatosInvalidosException.class, solicitud("0", null));
        actualizaRechaza(DatosInvalidosException.class,
                solicitud("0", Arrays.asList((ServicioAdicionalRequest) null)));
        actualizaRechaza(DatosInvalidosException.class, solicitud("0", List.of(servicio("  ", "1"))));
        actualizaRechaza(DatosInvalidosException.class, solicitud("0", List.of(servicio("x".repeat(101), "1"))));
        actualizaRechaza(DatosInvalidosException.class, solicitud("0", List.of(servicio("Desayuno", null))));
        actualizaRechaza(DatosInvalidosException.class, solicitud("0", List.of(servicio("Desayuno", "-1"))));
        actualizaRechaza(DatosInvalidosException.class,
                solicitud("0", List.of(servicio("Desayuno", "1"), servicio(" desayuno ", "2"))));
    }

    @Test
    void rechazaPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.consultarConfiguracion(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaUsuarioSinPermisos() {
        publicacionExistente();
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion)).thenReturn(Optional.empty());

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarConfiguracion(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaUsuarioQueNoEsAnfitrion() {
        publicacionExistente();
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(false, true, true, false, true, 0)));

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarConfiguracion(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaSiNoSeEncuentraElAlojamiento() {
        publicacionExistente();
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(true, true, true, false, true, 0)));
        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento)).thenReturn(Optional.empty());

        assertThrows(ConflictoNegocioException.class,
                () -> service.consultarConfiguracion(idAnfitrion, idPublicacion));
    }

    @Test
    void rechazaAlAnfitrionQueNoEsElPropietario() {
        publicacionExistente();
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(true, true, true, false, true, 0)));
        AlojamientoPublicacionInfo alojamiento = mock(AlojamientoPublicacionInfo.class);
        when(alojamiento.idAnfitrion()).thenReturn(UUID.randomUUID());
        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento)).thenReturn(Optional.of(alojamiento));

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.consultarConfiguracion(idAnfitrion, idPublicacion));
    }
}
