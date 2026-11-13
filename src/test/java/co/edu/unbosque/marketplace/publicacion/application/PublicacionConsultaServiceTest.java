
package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.MisPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionConsultaServiceTest {

    @Mock
    private AlojamientoPublicacionQuery alojamientoPublicacionQuery;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private ImagenPublicacionRepository imagenRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private ServicioPublicacionQuery servicioPublicacionQuery;

    private PublicacionConsultaService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idServicio = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PublicacionConsultaService(
                alojamientoPublicacionQuery,
                publicacionRepository,
                imagenRepository,
                publicacionServicioRepository,
                servicioPublicacionQuery
        );
    }

    private AlojamientoPublicacionInfo crearAlojamientoInfo() {

        AlojamientoPublicacionInfo alojamiento =
                mock(AlojamientoPublicacionInfo.class);

        when(alojamiento.idAlojamiento()).thenReturn(idAlojamiento);

        return alojamiento;
    }

    @Test
    void consultarMisPublicacionesConAnfitrionNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarMisPublicaciones(null)
        );

        verifyNoInteractions(alojamientoPublicacionQuery);
    }

    @Test
    void consultarMisPublicacionesSinAlojamientos() {

        when(alojamientoPublicacionQuery.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of());

        MisPublicacionesResponse respuesta =
                service.consultarMisPublicaciones(idAnfitrion);

        assertNotNull(respuesta);

        verify(alojamientoPublicacionQuery)
                .consultarPorAnfitrion(idAnfitrion);

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void consultarMisPublicacionesSinPublicaciones() {

        AlojamientoPublicacionInfo alojamiento =
                crearAlojamientoInfo();

        when(alojamientoPublicacionQuery.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of(alojamiento));

        when(publicacionRepository
                .findByIdAlojamientoInOrderByFechaCreacionDesc(
                        List.of(idAlojamiento)))
                .thenReturn(List.of());

        MisPublicacionesResponse respuesta =
                service.consultarMisPublicaciones(idAnfitrion);

        assertNotNull(respuesta);

        verify(publicacionRepository)
                .findByIdAlojamientoInOrderByFechaCreacionDesc(
                        List.of(idAlojamiento));

        verifyNoInteractions(imagenRepository);
        verifyNoInteractions(servicioPublicacionQuery);
    }

    @Test
    void consultarMisPublicacionesCorrectamente() {

        AlojamientoPublicacionInfo alojamiento =
                crearAlojamientoInfo();

        when(alojamiento.ciudad()).thenReturn("Bogotá");
        when(alojamiento.tipoAlojamiento()).thenReturn("Apartamento");
        when(alojamiento.capacidad()).thenReturn(3);

        Publicacion publicacion = mock(Publicacion.class);

        OffsetDateTime fecha = OffsetDateTime.now();

        when(alojamientoPublicacionQuery.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of(alojamiento));

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getTitulo()).thenReturn("Apartamento de prueba");
        when(publicacion.getPrecioPorNoche())
                .thenReturn(new BigDecimal("150000"));
        when(publicacion.getEstado()).thenReturn("BORRADOR");
        when(publicacion.getFechaCreacion()).thenReturn(fecha);

        when(publicacionRepository
                .findByIdAlojamientoInOrderByFechaCreacionDesc(
                        List.of(idAlojamiento)))
                .thenReturn(List.of(publicacion));

        MisPublicacionesResponse respuesta =
                service.consultarMisPublicaciones(idAnfitrion);

        assertNotNull(respuesta);

        verify(alojamientoPublicacionQuery)
                .consultarPorAnfitrion(idAnfitrion);

        verify(publicacionRepository)
                .findByIdAlojamientoInOrderByFechaCreacionDesc(
                        List.of(idAlojamiento));
    }

    @Test
    void consultarDetalleConIdentificadoresNulos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarDetalle(null, idPublicacion)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarDetalle(idAnfitrion, null)
        );

        verifyNoInteractions(alojamientoPublicacionQuery);
    }

    @Test
    void consultarDetalleSinAlojamientos() {

        when(alojamientoPublicacionQuery.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarDetalle(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void consultarDetalleConPublicacionInexistente() {

        AlojamientoPublicacionInfo alojamiento =
                crearAlojamientoInfo();

        when(alojamientoPublicacionQuery.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of(alojamiento));

        when(publicacionRepository.findByIdPublicacionAndIdAlojamientoIn(
                idPublicacion, List.of(idAlojamiento)))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarDetalle(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(imagenRepository);
        verifyNoInteractions(publicacionServicioRepository);
        verifyNoInteractions(servicioPublicacionQuery);
    }

    @Test
    void consultarDetalleCorrectamente() {

        AlojamientoPublicacionInfo alojamiento =
                crearAlojamientoInfo();

        when(alojamiento.ubicacion()).thenReturn("Calle 10");
        when(alojamiento.ciudad()).thenReturn("Bogotá");
        when(alojamiento.tipoAlojamiento()).thenReturn("Apartamento");
        when(alojamiento.capacidad()).thenReturn(3);

        Publicacion publicacion = mock(Publicacion.class);
        ImagenPublicacion imagen = mock(ImagenPublicacion.class);
        PublicacionServicio relacion = mock(PublicacionServicio.class);
        ServicioInfo servicio = mock(ServicioInfo.class);

        OffsetDateTime fecha = OffsetDateTime.now();
        UUID idImagen = UUID.randomUUID();

        when(alojamientoPublicacionQuery.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of(alojamiento));

        when(publicacionRepository.findByIdPublicacionAndIdAlojamientoIn(
                idPublicacion, List.of(idAlojamiento)))
                .thenReturn(Optional.of(publicacion));

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getIdPolitica()).thenReturn(UUID.randomUUID());
        when(publicacion.getTitulo()).thenReturn("Apartamento de prueba");
        when(publicacion.getDescripcion()).thenReturn("Descripción");
        when(publicacion.getReglas()).thenReturn("No fumar");
        when(publicacion.getPrecioPorNoche())
                .thenReturn(new BigDecimal("150000"));
        when(publicacion.getTarifaLimpieza())
                .thenReturn(new BigDecimal("20000"));
        when(publicacion.getModoAceptacion()).thenReturn("INMEDIATA");
        when(publicacion.getEstado()).thenReturn("BORRADOR");
        when(publicacion.getFechaCreacion()).thenReturn(fecha);
        when(publicacion.getFechaActualizacion()).thenReturn(fecha);

        when(imagen.getIdImagen()).thenReturn(idImagen);
        when(imagen.getRutaImagen()).thenReturn("imagenes/foto.jpg");
        when(imagen.getOrden()).thenReturn(1);
        when(imagen.isEsPortada()).thenReturn(true);

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of(imagen));

        when(relacion.getIdServicio()).thenReturn(idServicio);

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(relacion));

        when(servicio.idServicio()).thenReturn(idServicio);
        when(servicio.nombre()).thenReturn("WiFi");
        when(servicio.descripcion()).thenReturn("Internet inalámbrico");

        when(servicioPublicacionQuery.consultarPorIds(List.of(idServicio)))
                .thenReturn(List.of(servicio));

        PublicacionDetalleResponse respuesta =
                service.consultarDetalle(idAnfitrion, idPublicacion);

        assertNotNull(respuesta);

        verify(imagenRepository)
                .findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        verify(publicacionServicioRepository)
                .findByIdPublicacion(idPublicacion);

        verify(servicioPublicacionQuery)
                .consultarPorIds(List.of(idServicio));
    }
}
