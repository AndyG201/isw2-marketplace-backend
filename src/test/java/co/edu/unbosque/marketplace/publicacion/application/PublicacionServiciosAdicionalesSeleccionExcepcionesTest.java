package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionServiciosAdicionalesSeleccionExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private ServicioAdicionalQuery servicioAdicionalQuery;
    @InjectMocks private PublicacionServiciosAdicionalesSeleccionService service;

    private final UUID idPublicacion = UUID.randomUUID();

    private void publicacionActiva() {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getEstado()).thenReturn("ACTIVA");
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));
    }

    @Test
    void rechazaPublicacionNula() {
        assertThrows(DatosInvalidosException.class, () -> service.consultarDisponibles(null));
    }

    @Test
    void rechazaPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.consultarDisponibles(idPublicacion));
    }

    @Test
    void rechazaPublicacionNoActiva() {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getEstado()).thenReturn("PAUSADA");
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));

        assertThrows(RecursoNoEncontradoException.class, () -> service.consultarDisponibles(idPublicacion));
    }

    @Test
    void rechazaSeleccionesInvalidas() {
        publicacionActiva();
        UUID disponible = UUID.randomUUID();
        when(servicioAdicionalQuery.consultarActivosPorPublicacion(idPublicacion)).thenReturn(List.of(
                new ServicioAdicionalInfo(disponible, idPublicacion, "Desayuno", null, BigDecimal.TEN, true)));

        assertThrows(DatosInvalidosException.class, () -> service.validarSeleccion(idPublicacion, null));
        assertThrows(DatosInvalidosException.class,
                () -> service.validarSeleccion(idPublicacion, Arrays.asList(disponible, null)));
        assertThrows(DatosInvalidosException.class,
                () -> service.validarSeleccion(idPublicacion, List.of(disponible, disponible)));
        assertThrows(DatosInvalidosException.class,
                () -> service.validarSeleccion(idPublicacion, List.of(UUID.randomUUID())));
    }
}
