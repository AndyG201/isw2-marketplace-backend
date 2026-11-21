package co.edu.unbosque.marketplace.cotizacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CotizacionResponse;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CrearCotizacionRequest;
import co.edu.unbosque.marketplace.cotizacion.application.port.CotizacionAdicionalRepository;
import co.edu.unbosque.marketplace.cotizacion.application.port.CotizacionRepository;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.cotizacion.contracts.PublicacionCotizacionInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.PublicacionCotizacionQuery;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;
import co.edu.unbosque.marketplace.tarifa.contracts.ConfiguracionTarifaInfo;
import co.edu.unbosque.marketplace.tarifa.contracts.ConfiguracionTarifaQuery;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    private static final LocalDate ENTRADA = LocalDate.of(2030, 1, 10);
    private static final LocalDate SALIDA = LocalDate.of(2030, 1, 12);

    @Mock private CotizacionRepository cotizacionRepository;
    @Mock private CotizacionAdicionalRepository adicionalRepository;
    @Mock private PublicacionCotizacionQuery publicacionQuery;
    @Mock private CotizacionDisponibilidadQuery disponibilidadQuery;
    @Mock private ConfiguracionTarifaQuery tarifaQuery;
    @Mock private ServicioAdicionalQuery servicioAdicionalQuery;
    @InjectMocks private CotizacionService service;

    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();

    private CrearCotizacionRequest solicitud(LocalDate entrada, LocalDate salida, int huespedes, List<UUID> adicionales) {
        return new CrearCotizacionRequest(idPublicacion, entrada, salida, huespedes, adicionales);
    }

    private void publicacionActiva() {
        when(publicacionQuery.consultarPublicacionActiva(idPublicacion)).thenReturn(Optional.of(
                new PublicacionCotizacionInfo(idPublicacion, idAlojamiento,
                        new BigDecimal("100000"), new BigDecimal("20000"), 4)));
    }

    private void disponible() {
        when(disponibilidadQuery.estaDisponible(idAlojamiento, ENTRADA, SALIDA)).thenReturn(true);
    }

    private void tarifaVigente() {
        when(tarifaQuery.consultarConfiguracionVigente()).thenReturn(Optional.of(new ConfiguracionTarifaInfo(
                UUID.randomUUID(), new BigDecimal("10.00"), new BigDecimal("5.00"), OffsetDateTime.now())));
    }

    @Test
    void rechazaSolicitudIncompleta() {
        assertThrows(DatosInvalidosException.class, () -> service.calcular(null));
        assertThrows(DatosInvalidosException.class,
                () -> service.calcular(new CrearCotizacionRequest(null, ENTRADA, SALIDA, 2, null)));
        assertThrows(DatosInvalidosException.class, () -> service.calcular(solicitud(null, SALIDA, 2, null)));
        assertThrows(DatosInvalidosException.class, () -> service.calcular(solicitud(ENTRADA, null, 2, null)));
    }

    @Test
    void rechazaSalidaNoPosteriorALaEntrada() {
        assertThrows(DatosInvalidosException.class, () -> service.calcular(solicitud(ENTRADA, ENTRADA, 2, null)));
    }

    @Test
    void rechazaCantidadDeHuespedesMenorAUno() {
        assertThrows(DatosInvalidosException.class, () -> service.calcular(solicitud(ENTRADA, SALIDA, 0, null)));
    }

    @Test
    void rechazaPublicacionInexistenteOInactiva() {
        when(publicacionQuery.consultarPublicacionActiva(idPublicacion)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.calcular(solicitud(ENTRADA, SALIDA, 2, null)));
    }

    @Test
    void rechazaMasHuespedesQueLaCapacidad() {
        publicacionActiva();

        assertThrows(DatosInvalidosException.class, () -> service.calcular(solicitud(ENTRADA, SALIDA, 5, null)));
    }

    @Test
    void rechazaFechasNoDisponibles() {
        publicacionActiva();
        when(disponibilidadQuery.estaDisponible(idAlojamiento, ENTRADA, SALIDA)).thenReturn(false);

        assertThrows(ConflictoNegocioException.class, () -> service.calcular(solicitud(ENTRADA, SALIDA, 2, null)));
    }

    @Test
    void rechazaSiNoHayTarifaVigente() {
        publicacionActiva();
        disponible();
        when(tarifaQuery.consultarConfiguracionVigente()).thenReturn(Optional.empty());

        assertThrows(ConflictoNegocioException.class, () -> service.calcular(solicitud(ENTRADA, SALIDA, 2, null)));
        verify(cotizacionRepository, never()).save(any());
    }

    @Test
    void rechazaAdicionalesNulosOIdenticos() {
        publicacionActiva();
        disponible();
        tarifaVigente();
        UUID repetido = UUID.randomUUID();

        assertThrows(DatosInvalidosException.class,
                () -> service.calcular(solicitud(ENTRADA, SALIDA, 2, Arrays.asList(UUID.randomUUID(), null))));
        assertThrows(DatosInvalidosException.class,
                () -> service.calcular(solicitud(ENTRADA, SALIDA, 2, List.of(repetido, repetido))));
    }

    @Test
    void rechazaAdicionalesQueNoEstanActivosEnLaPublicacion() {
        publicacionActiva();
        disponible();
        tarifaVigente();
        when(servicioAdicionalQuery.consultarActivosPorPublicacion(idPublicacion)).thenReturn(List.of());

        assertThrows(DatosInvalidosException.class,
                () -> service.calcular(solicitud(ENTRADA, SALIDA, 2, List.of(UUID.randomUUID()))));
    }

    @Test
    void calculaElTotalConAdicionales() {
        publicacionActiva();
        disponible();
        tarifaVigente();
        UUID idAdicional = UUID.randomUUID();
        when(servicioAdicionalQuery.consultarActivosPorPublicacion(idPublicacion)).thenReturn(List.of(
                new ServicioAdicionalInfo(idAdicional, idPublicacion, "Desayuno", null, new BigDecimal("30000"), true)));

        CotizacionResponse respuesta = service.calcular(solicitud(ENTRADA, SALIDA, 2, List.of(idAdicional)));

        // 2 noches x 100000 = 200000; servicio 10% = 20000; descuento 5% = 10000; limpieza 20000; adicional 30000
        assertEquals(0, new BigDecimal("260000.00").compareTo(respuesta.total()));
        assertEquals(1, respuesta.serviciosAdicionales().size());
        verify(cotizacionRepository).save(any());
        verify(adicionalRepository).saveAll(any());
    }
}
