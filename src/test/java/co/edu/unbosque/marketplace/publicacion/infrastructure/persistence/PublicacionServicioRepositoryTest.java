package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;

class PublicacionServicioRepositoryTest {

    private PublicacionServicioRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(PublicacionServicioRepository.class);
    }

    @Test
    void debeEncontrarServiciosPorIdPublicacion() {
        UUID idPublicacion = UUID.randomUUID();

        PublicacionServicio servicio1 = mock(PublicacionServicio.class);
        PublicacionServicio servicio2 = mock(PublicacionServicio.class);

        List<PublicacionServicio> servicios = List.of(
            servicio1,
            servicio2
        );

        when(repository.findByIdPublicacion(idPublicacion))
            .thenReturn(servicios);

        List<PublicacionServicio> resultado =
            repository.findByIdPublicacion(idPublicacion);

        assertAll(
            () -> assertNotNull(resultado),
            () -> assertEquals(2, resultado.size()),
            () -> assertEquals(servicios, resultado)
        );

        verify(repository).findByIdPublicacion(idPublicacion);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoExistenServicios() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.findByIdPublicacion(idPublicacion))
            .thenReturn(List.of());

        List<PublicacionServicio> resultado =
            repository.findByIdPublicacion(idPublicacion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository).findByIdPublicacion(idPublicacion);
    }

    @Test
    void debeRetornarTrueCuandoExisteLaRelacionPublicacionServicio() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idServicio = UUID.randomUUID();

        when(repository.existsByIdPublicacionAndIdServicio(
            idPublicacion, idServicio
        )).thenReturn(true);

        boolean resultado =
            repository.existsByIdPublicacionAndIdServicio(
                idPublicacion, idServicio
            );

        assertTrue(resultado);

        verify(repository).existsByIdPublicacionAndIdServicio(
            idPublicacion, idServicio
        );
    }

    @Test
    void debeRetornarFalseCuandoNoExisteLaRelacionPublicacionServicio() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idServicio = UUID.randomUUID();

        when(repository.existsByIdPublicacionAndIdServicio(
            idPublicacion, idServicio
        )).thenReturn(false);

        boolean resultado =
            repository.existsByIdPublicacionAndIdServicio(
                idPublicacion, idServicio
            );

        assertFalse(resultado);

        verify(repository).existsByIdPublicacionAndIdServicio(
            idPublicacion, idServicio
        );
    }
}
