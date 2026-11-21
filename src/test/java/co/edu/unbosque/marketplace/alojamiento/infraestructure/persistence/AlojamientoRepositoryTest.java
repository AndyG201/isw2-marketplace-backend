package co.edu.unbosque.marketplace.alojamiento.infraestructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.application.port.AlojamientoRepository;
import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;



@ExtendWith(MockitoExtension.class)
class AlojamientoRepositoryTest {

    @Mock
    private AlojamientoRepository alojamientoRepository;

    private Alojamiento alojamiento;
    private UUID idAlojamiento;
    private UUID idAnfitrion;

    @BeforeEach
    void setUp() {
        idAlojamiento = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();

        alojamiento = new Alojamiento(
                idAlojamiento,
                idAnfitrion,
                "Calle 100 #15-20",
                "Bogotá",
                "Apartamento",
                4,
                OffsetDateTime.now()
        );
    }

    @Test
    void debeGuardarAlojamiento() {
        when(alojamientoRepository.save(alojamiento))
                .thenReturn(alojamiento);

        Alojamiento resultado = alojamientoRepository.save(alojamiento);

        assertNotNull(resultado);
        assertEquals(idAlojamiento, resultado.getIdAlojamiento());
        assertEquals(idAnfitrion, resultado.getIdAnfitrion());

        verify(alojamientoRepository).save(alojamiento);
    }

    @Test
    void debeBuscarAlojamientoPorId() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        Optional<Alojamiento> resultado =
                alojamientoRepository.findById(idAlojamiento);

        assertTrue(resultado.isPresent());
        assertEquals(idAlojamiento, resultado.get().getIdAlojamiento());

        verify(alojamientoRepository).findById(idAlojamiento);
    }

    @Test
    void debeRetornarVacioCuandoNoEncuentraAlojamiento() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        Optional<Alojamiento> resultado =
                alojamientoRepository.findById(idAlojamiento);

        assertTrue(resultado.isEmpty());

        verify(alojamientoRepository).findById(idAlojamiento);
    }

    @Test
    void debeConsultarAlojamientosPorAnfitrion() {
        Alojamiento segundoAlojamiento = new Alojamiento(
                UUID.randomUUID(),
                idAnfitrion,
                "Carrera 7 #20-30",
                "Bogotá",
                "Casa",
                6,
                OffsetDateTime.now()
        );

        when(alojamientoRepository.findByIdAnfitrion(idAnfitrion))
                .thenReturn(List.of(alojamiento, segundoAlojamiento));

        List<Alojamiento> resultado =
                alojamientoRepository.findByIdAnfitrion(idAnfitrion);

        assertEquals(2, resultado.size());
        assertEquals(idAnfitrion, resultado.get(0).getIdAnfitrion());
        assertEquals(idAnfitrion, resultado.get(1).getIdAnfitrion());

        verify(alojamientoRepository).findByIdAnfitrion(idAnfitrion);
    }

    @Test
    void debeRetornarListaVaciaCuandoAnfitrionNoTieneAlojamientos() {
        when(alojamientoRepository.findByIdAnfitrion(idAnfitrion))
                .thenReturn(List.of());

        List<Alojamiento> resultado =
                alojamientoRepository.findByIdAnfitrion(idAnfitrion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(alojamientoRepository).findByIdAnfitrion(idAnfitrion);
    }

    @Test
    void debeEliminarAlojamientoPorId() {
        doNothing().when(alojamientoRepository)
                .deleteById(idAlojamiento);

        alojamientoRepository.deleteById(idAlojamiento);

        verify(alojamientoRepository).deleteById(idAlojamiento);
    }

    @Test
    void debeConsultarTodosLosAlojamientos() {
        when(alojamientoRepository.findAll())
                .thenReturn(List.of(alojamiento));

        List<Alojamiento> resultado = alojamientoRepository.findAll();

        assertEquals(1, resultado.size());
        assertEquals(idAlojamiento, resultado.get(0).getIdAlojamiento());

        verify(alojamientoRepository).findAll();
    }
}
