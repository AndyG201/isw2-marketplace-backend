package co.edu.unbosque.marketplace.tarifa.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfiguracionTarifaResponse;
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfigurarPorcentajesTarifaRequest;
import co.edu.unbosque.marketplace.tarifa.application.port.ConfiguracionTarifaRepository;
import co.edu.unbosque.marketplace.tarifa.domain.ConfiguracionTarifa;

@ExtendWith(MockitoExtension.class)
class ConfiguracionTarifaServiceTest {

    @Mock
    private ConfiguracionTarifaRepository repository;

    @InjectMocks
    private ConfiguracionTarifaService service;

    private static ConfigurarPorcentajesTarifaRequest solicitud(String servicio, String descuento) {
        return new ConfigurarPorcentajesTarifaRequest(
                servicio == null ? null : new BigDecimal(servicio),
                descuento == null ? null : new BigDecimal(descuento));
    }

    @Test
    void consultarParaAdministracionFallaSiNoHayConfiguracionVigente() {
        when(repository.findFirstByVigenteHastaIsNullAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(any()))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.consultarParaAdministracion());
    }

    @Test
    void configurarExigeAdministrador() {
        assertThrows(DatosInvalidosException.class,
                () -> service.configurar(null, solicitud("10.00", "5.00")));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void configurarExigeSolicitud() {
        assertThrows(DatosInvalidosException.class, () -> service.configurar(UUID.randomUUID(), null));
    }

    @Test
    void configurarRechazaPorcentajesFueraDeRango() {
        UUID admin = UUID.randomUUID();

        assertThrows(DatosInvalidosException.class, () -> service.configurar(admin, solicitud(null, "5.00")));
        assertThrows(DatosInvalidosException.class, () -> service.configurar(admin, solicitud("-1.00", "5.00")));
        assertThrows(DatosInvalidosException.class, () -> service.configurar(admin, solicitud("100.01", "5.00")));
        assertThrows(DatosInvalidosException.class, () -> service.configurar(admin, solicitud("10.123", "5.00")));
        assertThrows(DatosInvalidosException.class, () -> service.configurar(admin, solicitud("10.00", "101.00")));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void configurarCierraLaConfiguracionAnteriorYCreaLaNueva() {
        UUID admin = UUID.randomUUID();
        ConfiguracionTarifa anterior = new ConfiguracionTarifa(UUID.randomUUID(), admin,
                new BigDecimal("8.00"), new BigDecimal("2.00"), null, null, null);
        when(repository.bloquearConfiguracionesVigentes(any())).thenReturn(List.of(anterior));
        when(repository.saveAndFlush(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        ConfiguracionTarifaResponse respuesta = service.configurar(admin, solicitud("10.00", "5.00"));

        assertEquals(new BigDecimal("10.00"), respuesta.porcentajeServicio());
        assertEquals(new BigDecimal("5.00"), respuesta.porcentajeDescuento());
        assertEquals(admin, respuesta.idAdministrador());
        verify(repository).saveAll(List.of(anterior));
        verify(repository).flush();
    }
}
