package co.edu.unbosque.marketplace.tarifa.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.tarifa.api.request.ConfigurarPorcentajesTarifaRequest;
import co.edu.unbosque.marketplace.tarifa.api.response.ConfiguracionTarifaResponse;
import co.edu.unbosque.marketplace.tarifa.contracts.ConfiguracionTarifaInfo;
import co.edu.unbosque.marketplace.tarifa.contracts.ConfiguracionTarifaQuery;
import co.edu.unbosque.marketplace.tarifa.domain.ConfiguracionTarifa;
import co.edu.unbosque.marketplace.tarifa.infrastructure.persistence.ConfiguracionTarifaRepository;

@Service
public class ConfiguracionTarifaService
        implements ConfiguracionTarifaQuery {

    private final ConfiguracionTarifaRepository repository;

    public ConfiguracionTarifaService(
            ConfiguracionTarifaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionTarifaInfo> consultarConfiguracionVigente() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        return repository
                .findFirstByVigenteHastaIsNullAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
                        ahora)
                .map(this::convertirContrato);
    }

    @Transactional(readOnly = true)
    public ConfiguracionTarifaResponse consultarParaAdministracion() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        ConfiguracionTarifa vigente = repository
                .findFirstByVigenteHastaIsNullAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
                        ahora)
                .orElseThrow(() -> new NoSuchElementException(
                        "No hay una configuración de tarifas vigente."
                ));

        return convertirRespuesta(vigente);
    }

    @Transactional
    public ConfiguracionTarifaResponse configurar(
            UUID idAdministrador,
            ConfigurarPorcentajesTarifaRequest solicitud) {

        if (idAdministrador == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al administrador."
            );
        }

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La configuración de tarifas es obligatoria."
            );
        }

        validarPorcentaje(
                solicitud.porcentajeServicio(),
                "El porcentaje de servicio"
        );
        validarPorcentaje(
                solicitud.porcentajeDescuento(),
                "El porcentaje de descuento"
        );

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        List<ConfiguracionTarifa> anteriores =
                repository.bloquearConfiguracionesVigentes(ahora);

        for (ConfiguracionTarifa anterior : anteriores) {
            anterior.setVigenteHasta(ahora);
        }

        if (!anteriores.isEmpty()) {
            repository.saveAll(anteriores);
            repository.flush();
        }

        ConfiguracionTarifa nueva = new ConfiguracionTarifa(
                UUID.randomUUID(),
                idAdministrador,
                solicitud.porcentajeServicio(),
                solicitud.porcentajeDescuento(),
                ahora,
                null,
                ahora
        );

        return convertirRespuesta(repository.saveAndFlush(nueva));
    }

    private static void validarPorcentaje(
            BigDecimal porcentaje,
            String nombre) {
        if (porcentaje == null
                || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(new BigDecimal("100.00")) > 0
                || porcentaje.scale() > 2) {
            throw new IllegalArgumentException(
                    nombre + " debe estar entre 0 y 100, con máximo dos decimales."
            );
        }
    }

    private ConfiguracionTarifaInfo convertirContrato(
            ConfiguracionTarifa configuracion) {
        return new ConfiguracionTarifaInfo(
                configuracion.getIdConfiguracion(),
                configuracion.getPorcentajeServicio(),
                configuracion.getPorcentajeDescuento(),
                configuracion.getVigenteDesde()
        );
    }

    private ConfiguracionTarifaResponse convertirRespuesta(
            ConfiguracionTarifa configuracion) {
        return new ConfiguracionTarifaResponse(
                configuracion.getIdConfiguracion(),
                configuracion.getIdAdministrador(),
                configuracion.getPorcentajeServicio(),
                configuracion.getPorcentajeDescuento(),
                configuracion.getVigenteDesde(),
                configuracion.getVigenteHasta(),
                configuracion.getFechaCreacion()
        );
    }
}