package co.edu.unbosque.marketplace.alojamiento.contracts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlojamientoPublicacionQueryTest {

    private AlojamientoPublicacionQuery consulta;

    private UUID idAlojamiento;
    private UUID idAnfitrion;

    @BeforeEach
    void setUp() {
        consulta = mock(AlojamientoPublicacionQuery.class);
        idAlojamiento = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
    }

    @Test
    void debeConsultarIdAnfitrion() {
        when(consulta.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));

        Optional<UUID> resultado =
                consulta.consultarIdAnfitrion(idAlojamiento);

        assertTrue(resultado.isPresent());
        assertEquals(idAnfitrion, resultado.get());

        verify(consulta).consultarIdAnfitrion(idAlojamiento);
    }

    @Test
    void debeRetornarVacioCuandoNoEncuentraAnfitrion() {
        when(consulta.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.empty());

        Optional<UUID> resultado =
                consulta.consultarIdAnfitrion(idAlojamiento);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeConsultarDatosParaPublicacion() {
        AlojamientoPublicacionInfo informacion =
                new AlojamientoPublicacionInfo(
                        idAlojamiento,
                        idAnfitrion,
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        when(consulta.consultarDatosParaPublicacion(idAlojamiento))
                .thenReturn(Optional.of(informacion));

        Optional<AlojamientoPublicacionInfo> resultado =
                consulta.consultarDatosParaPublicacion(idAlojamiento);

        assertTrue(resultado.isPresent());
        assertEquals(informacion, resultado.get());

        verify(consulta).consultarDatosParaPublicacion(idAlojamiento);
    }

    @Test
    void debeRetornarVacioCuandoNoEncuentraDatosParaPublicacion() {
        when(consulta.consultarDatosParaPublicacion(idAlojamiento))
                .thenReturn(Optional.empty());

        Optional<AlojamientoPublicacionInfo> resultado =
                consulta.consultarDatosParaPublicacion(idAlojamiento);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeConsultarAlojamientosPorAnfitrion() {
        AlojamientoPublicacionInfo informacion1 =
                new AlojamientoPublicacionInfo(
                        idAlojamiento,
                        idAnfitrion,
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        AlojamientoPublicacionInfo informacion2 =
                new AlojamientoPublicacionInfo(
                        UUID.randomUUID(),
                        idAnfitrion,
                        "Carrera 15",
                        "Medellín",
                        "Casa",
                        6
                );

        List<AlojamientoPublicacionInfo> alojamientos =
                List.of(informacion1, informacion2);

        when(consulta.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(alojamientos);

        List<AlojamientoPublicacionInfo> resultado =
                consulta.consultarPorAnfitrion(idAnfitrion);

        assertEquals(2, resultado.size());
        assertEquals(informacion1, resultado.get(0));
        assertEquals(informacion2, resultado.get(1));

        verify(consulta).consultarPorAnfitrion(idAnfitrion);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayAlojamientosDelAnfitrion() {
        when(consulta.consultarPorAnfitrion(idAnfitrion))
                .thenReturn(List.of());

        List<AlojamientoPublicacionInfo> resultado =
                consulta.consultarPorAnfitrion(idAnfitrion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeConsultarAlojamientosPorIds() {
        UUID segundoId = UUID.randomUUID();

        List<UUID> ids = List.of(idAlojamiento, segundoId);

        AlojamientoPublicacionInfo informacion1 =
                new AlojamientoPublicacionInfo(
                        idAlojamiento,
                        idAnfitrion,
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        AlojamientoPublicacionInfo informacion2 =
                new AlojamientoPublicacionInfo(
                        segundoId,
                        idAnfitrion,
                        "Carrera 15",
                        "Medellín",
                        "Casa",
                        6
                );

        when(consulta.consultarPorIdsAlojamiento(ids))
                .thenReturn(List.of(informacion1, informacion2));

        List<AlojamientoPublicacionInfo> resultado =
                consulta.consultarPorIdsAlojamiento(ids);

        assertEquals(2, resultado.size());
        assertEquals(informacion1, resultado.get(0));
        assertEquals(informacion2, resultado.get(1));

        verify(consulta).consultarPorIdsAlojamiento(ids);
    }

    @Test
    void debeRetornarListaVaciaCuandoNoEncuentraIds() {
        List<UUID> ids = List.of(idAlojamiento);

        when(consulta.consultarPorIdsAlojamiento(ids))
                .thenReturn(List.of());

        List<AlojamientoPublicacionInfo> resultado =
                consulta.consultarPorIdsAlojamiento(ids);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
