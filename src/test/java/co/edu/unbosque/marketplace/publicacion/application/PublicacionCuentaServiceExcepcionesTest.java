package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;

@ExtendWith(MockitoExtension.class)
class PublicacionCuentaServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private AlojamientoPublicacionQuery alojamientoQuery;
    @InjectMocks private PublicacionCuentaService service;

    @Test
    void rechazaUsuarioOFechaNulos() {
        assertThrows(DatosInvalidosException.class,
                () -> service.pausarPublicacionesActivas(null, OffsetDateTime.now()));
        assertThrows(DatosInvalidosException.class,
                () -> service.pausarPublicacionesActivas(UUID.randomUUID(), null));
    }
}
