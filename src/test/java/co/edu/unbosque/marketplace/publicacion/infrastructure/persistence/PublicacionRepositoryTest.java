package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

class PublicacionRepositoryTest {

    private PublicacionRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(PublicacionRepository.class);
    }

    @Test
    void debeEncontrarPublicacionPorIdAlojamiento() {
        UUID idAlojamiento = UUID.randomUUID();
        Publicacion publicacion = mock(Publicacion.class);

        when(repository.findByIdAlojamiento(idAlojamiento))
            .thenReturn(Optional.of(publicacion));

        Optional<Publicacion> resultado =
            repository.findByIdAlojamiento(idAlojamiento);

        assertTrue(resultado.isPresent());
        assertEquals(publicacion, resultado.get());

        verify(repository).findByIdAlojamiento(idAlojamiento);
    }

    @Test
    void debeRetornarVacioCuandoNoExistePublicacionPorAlojamiento() {
        UUID idAlojamiento = UUID.randomUUID();

        when(repository.findByIdAlojamiento(idAlojamiento))
            .thenReturn(Optional.empty());

        Optional<Publicacion> resultado =
            repository.findByIdAlojamiento(idAlojamiento);

        assertTrue(resultado.isEmpty());

        verify(repository).findByIdAlojamiento(idAlojamiento);
    }

    @Test
    void debeEncontrarPublicacionesPorVariosAlojamientos() {
        List<UUID> idsAlojamiento = List.of(
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        Publicacion publicacion1 = mock(Publicacion.class);
        Publicacion publicacion2 = mock(Publicacion.class);

        List<Publicacion> publicaciones = List.of(
            publicacion1,
            publicacion2
        );

        when(repository.findByIdAlojamientoInOrderByFechaCreacionDesc(idsAlojamiento))
            .thenReturn(publicaciones);

        List<Publicacion> resultado =
            repository.findByIdAlojamientoInOrderByFechaCreacionDesc(idsAlojamiento);

        assertEquals(2, resultado.size());
        assertEquals(publicaciones, resultado);

        verify(repository)
            .findByIdAlojamientoInOrderByFechaCreacionDesc(idsAlojamiento);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayPublicacionesParaLosAlojamientos() {
        List<UUID> idsAlojamiento = List.of(UUID.randomUUID());

        when(repository.findByIdAlojamientoInOrderByFechaCreacionDesc(idsAlojamiento))
            .thenReturn(List.of());

        List<Publicacion> resultado =
            repository.findByIdAlojamientoInOrderByFechaCreacionDesc(idsAlojamiento);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository)
            .findByIdAlojamientoInOrderByFechaCreacionDesc(idsAlojamiento);
    }

    @Test
    void debeEncontrarPublicacionPorIdYListaDeAlojamientos() {
        UUID idPublicacion = UUID.randomUUID();
        List<UUID> idsAlojamiento = List.of(UUID.randomUUID());

        Publicacion publicacion = mock(Publicacion.class);

        when(repository.findByIdPublicacionAndIdAlojamientoIn(
            idPublicacion, idsAlojamiento
        )).thenReturn(Optional.of(publicacion));

        Optional<Publicacion> resultado =
            repository.findByIdPublicacionAndIdAlojamientoIn(
                idPublicacion, idsAlojamiento
            );

        assertTrue(resultado.isPresent());
        assertEquals(publicacion, resultado.get());

        verify(repository)
            .findByIdPublicacionAndIdAlojamientoIn(idPublicacion, idsAlojamiento);
    }

    @Test
    void debeRetornarVacioCuandoLaPublicacionNoPerteneceALosAlojamientos() {
        UUID idPublicacion = UUID.randomUUID();
        List<UUID> idsAlojamiento = List.of(UUID.randomUUID());

        when(repository.findByIdPublicacionAndIdAlojamientoIn(
            idPublicacion, idsAlojamiento
        )).thenReturn(Optional.empty());

        Optional<Publicacion> resultado =
            repository.findByIdPublicacionAndIdAlojamientoIn(
                idPublicacion, idsAlojamiento
            );

        assertTrue(resultado.isEmpty());

        verify(repository)
            .findByIdPublicacionAndIdAlojamientoIn(idPublicacion, idsAlojamiento);
    }

    @Test
    void debePausarPublicacionesActivasDeLosAlojamientosIndicados() {
        List<UUID> idsAlojamientos = List.of(
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        OffsetDateTime fechaActualizacion =
            OffsetDateTime.parse("2026-11-20T11:00:00-05:00");

        when(repository.pausarPublicacionesActivasPorAlojamientos(
            idsAlojamientos, fechaActualizacion
        )).thenReturn(2);

        int resultado =
            repository.pausarPublicacionesActivasPorAlojamientos(
                idsAlojamientos, fechaActualizacion
            );

        assertEquals(2, resultado);

        verify(repository).pausarPublicacionesActivasPorAlojamientos(
            idsAlojamientos, fechaActualizacion
        );
    }

    @Test
    void debeRetornarCeroCuandoNoHayPublicacionesActivasParaPausar() {
        List<UUID> idsAlojamientos = List.of(UUID.randomUUID());

        OffsetDateTime fechaActualizacion = OffsetDateTime.now();

        when(repository.pausarPublicacionesActivasPorAlojamientos(
            idsAlojamientos, fechaActualizacion
        )).thenReturn(0);

        int resultado =
            repository.pausarPublicacionesActivasPorAlojamientos(
                idsAlojamientos, fechaActualizacion
            );

        assertEquals(0, resultado);

        verify(repository).pausarPublicacionesActivasPorAlojamientos(
            idsAlojamientos, fechaActualizacion
        );
    }

    @Test
    void debeEncontrarPublicacionesPorEstado() {
        String estado = "ACTIVA";

        Publicacion publicacion1 = mock(Publicacion.class);
        Publicacion publicacion2 = mock(Publicacion.class);

        List<Publicacion> publicaciones = List.of(
            publicacion1,
            publicacion2
        );

        when(repository.findByEstadoOrderByFechaCreacionDesc(estado))
            .thenReturn(publicaciones);

        List<Publicacion> resultado =
            repository.findByEstadoOrderByFechaCreacionDesc(estado);

        assertEquals(2, resultado.size());
        assertEquals(publicaciones, resultado);

        verify(repository).findByEstadoOrderByFechaCreacionDesc(estado);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayPublicacionesConElEstadoIndicado() {
        String estado = "PAUSADA";

        when(repository.findByEstadoOrderByFechaCreacionDesc(estado))
            .thenReturn(List.of());

        List<Publicacion> resultado =
            repository.findByEstadoOrderByFechaCreacionDesc(estado);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository).findByEstadoOrderByFechaCreacionDesc(estado);
    }
}
