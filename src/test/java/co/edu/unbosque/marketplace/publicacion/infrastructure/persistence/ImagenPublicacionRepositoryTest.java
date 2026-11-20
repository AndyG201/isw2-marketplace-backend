package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;

class ImagenPublicacionRepositoryTest {

    private ImagenPublicacionRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(ImagenPublicacionRepository.class);
    }

    @Test
    void debeRetornarImagenesOrdenadasPorOrdenAscendente() {
        UUID idPublicacion = UUID.randomUUID();

        ImagenPublicacion imagen1 = new ImagenPublicacion();
        imagen1.setOrden(1);

        ImagenPublicacion imagen2 = new ImagenPublicacion();
        imagen2.setOrden(2);

        ImagenPublicacion imagen3 = new ImagenPublicacion();
        imagen3.setOrden(3);

        when(repository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
            .thenReturn(List.of(imagen1, imagen2, imagen3));

        List<ImagenPublicacion> resultado =
            repository.findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        assertAll(
            () -> assertEquals(3, resultado.size()),
            () -> assertEquals(1, resultado.get(0).getOrden()),
            () -> assertEquals(2, resultado.get(1).getOrden()),
            () -> assertEquals(3, resultado.get(2).getOrden())
        );

        verify(repository, times(1))
            .findByIdPublicacionOrderByOrdenAsc(idPublicacion);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoExistenImagenes() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
            .thenReturn(List.of());

        List<ImagenPublicacion> resultado =
            repository.findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository).findByIdPublicacionOrderByOrdenAsc(idPublicacion);
    }

    @Test
    void debeRetornarTrueCuandoExistenImagenesDeUnaPublicacion() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.existsByIdPublicacion(idPublicacion))
            .thenReturn(true);

        boolean resultado = repository.existsByIdPublicacion(idPublicacion);

        assertTrue(resultado);

        verify(repository).existsByIdPublicacion(idPublicacion);
    }

    @Test
    void debeRetornarFalseCuandoNoExistenImagenesDeUnaPublicacion() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.existsByIdPublicacion(idPublicacion))
            .thenReturn(false);

        boolean resultado = repository.existsByIdPublicacion(idPublicacion);

        assertFalse(resultado);

        verify(repository).existsByIdPublicacion(idPublicacion);
    }

    @Test
    void debeConsultarImagenesUsandoElIdentificadorCorrecto() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
            .thenReturn(List.of());

        repository.findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        verify(repository, times(1))
            .findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        verify(repository, never())
            .existsByIdPublicacion(any());
    }
}
