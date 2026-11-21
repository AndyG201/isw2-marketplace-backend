package co.edu.unbosque.marketplace.publicacion.application.port;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;

class RevisionPublicacionRepositoryTest {

    private RevisionPublicacionRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(RevisionPublicacionRepository.class);
    }

    @Test
    void debeEncontrarRevisionesPorIdPublicacion() {
        UUID idPublicacion = UUID.randomUUID();

        RevisionPublicacion revision1 = mock(RevisionPublicacion.class);
        RevisionPublicacion revision2 = mock(RevisionPublicacion.class);

        List<RevisionPublicacion> revisiones = List.of(
            revision1,
            revision2
        );

        when(repository.findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion))
            .thenReturn(revisiones);

        List<RevisionPublicacion> resultado =
            repository.findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion);

        assertAll(
            () -> assertNotNull(resultado),
            () -> assertEquals(2, resultado.size()),
            () -> assertEquals(revisiones, resultado)
        );

        verify(repository)
            .findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoExistenRevisiones() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion))
            .thenReturn(List.of());

        List<RevisionPublicacion> resultado =
            repository.findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository)
            .findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion);
    }

    @Test
    void debeConsultarRevisionesUsandoElIdentificadorCorrecto() {
        UUID idPublicacion = UUID.randomUUID();

        when(repository.findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion))
            .thenReturn(List.of());

        repository.findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion);

        verify(repository, times(1))
            .findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion);

        verifyNoMoreInteractions(repository);
    }
}
