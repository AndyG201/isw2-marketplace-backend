package co.edu.unbosque.marketplace.cotizacion.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.cotizacion.application.dto.CrearCotizacionRequest;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CotizacionAdicionalResponse;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CotizacionResponse;
import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;
import co.edu.unbosque.marketplace.cotizacion.domain.CotizacionAdicional;
import co.edu.unbosque.marketplace.cotizacion.application.port.CotizacionAdicionalRepository;
import co.edu.unbosque.marketplace.cotizacion.application.port.CotizacionRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;
import co.edu.unbosque.marketplace.tarifa.contracts.ConfiguracionTarifaInfo;
import co.edu.unbosque.marketplace.tarifa.contracts.ConfiguracionTarifaQuery;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.cotizacion.contracts.PublicacionCotizacionInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.PublicacionCotizacionQuery;
@Service
public class CotizacionService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final CotizacionRepository cotizacionRepository;
    private final CotizacionAdicionalRepository adicionalRepository;
    private final PublicacionCotizacionQuery publicacionQuery;
    private final CotizacionDisponibilidadQuery disponibilidadQuery;
    private final ConfiguracionTarifaQuery tarifaQuery;
    private final ServicioAdicionalQuery servicioAdicionalQuery;

    public CotizacionService(
            CotizacionRepository cotizacionRepository,
            CotizacionAdicionalRepository adicionalRepository,
            PublicacionCotizacionQuery publicacionQuery,
            CotizacionDisponibilidadQuery disponibilidadQuery,
            ConfiguracionTarifaQuery tarifaQuery,
            ServicioAdicionalQuery servicioAdicionalQuery) {
        this.cotizacionRepository = cotizacionRepository;
        this.adicionalRepository = adicionalRepository;
        this.publicacionQuery = publicacionQuery;
        this.disponibilidadQuery = disponibilidadQuery;
        this.tarifaQuery = tarifaQuery;
        this.servicioAdicionalQuery = servicioAdicionalQuery;
    }

    @Transactional
    public CotizacionResponse calcular(CrearCotizacionRequest solicitud) {
        validarSolicitud(solicitud);

        PublicacionCotizacionInfo publicacion = publicacionQuery
                .consultarPublicacionActiva(solicitud.idPublicacion())
                .orElseThrow(() -> new NoSuchElementException(
                        "La publicación no existe o no está activa."
                ));

        if (solicitud.cantidadHuespedes() > publicacion.capacidad()) {
            throw new IllegalArgumentException(
                    "La cantidad de huéspedes supera la capacidad del alojamiento."
            );
        }

        if (!disponibilidadQuery.estaDisponible(
                publicacion.idAlojamiento(),
                solicitud.fechaEntrada(),
                solicitud.fechaSalida())) {
            throw new IllegalStateException(
                    "El alojamiento no está disponible para las fechas indicadas."
            );
        }

        ConfiguracionTarifaInfo configuracion = tarifaQuery
                .consultarConfiguracionVigente()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay una configuración de tarifas vigente."
                ));

        List<ServicioAdicionalInfo> adicionalesSeleccionados =
                validarAdicionales(
                        solicitud.idPublicacion(),
                        solicitud.idsAdicionales()
                );

        long noches = ChronoUnit.DAYS.between(
                solicitud.fechaEntrada(),
                solicitud.fechaSalida()
        );

        BigDecimal precioPorNoche = moneda(publicacion.precioPorNoche());
        BigDecimal subtotalAlojamiento = moneda(
                precioPorNoche.multiply(BigDecimal.valueOf(noches))
        );
        BigDecimal tarifaLimpieza = moneda(publicacion.tarifaLimpieza());

        BigDecimal costoServicio = calcularPorcentaje(
                subtotalAlojamiento,
                configuracion.porcentajeServicio()
        );
        BigDecimal montoDescuento = calcularPorcentaje(
                subtotalAlojamiento,
                configuracion.porcentajeDescuento()
        );

        UUID idCotizacion = UUID.randomUUID();
        List<CotizacionAdicional> entidadesAdicionales = new ArrayList<>();
        List<CotizacionAdicionalResponse> respuestasAdicionales =
                new ArrayList<>();
        BigDecimal costoAdicionalTotal = BigDecimal.ZERO.setScale(2);

        for (ServicioAdicionalInfo adicional : adicionalesSeleccionados) {
            BigDecimal costo = moneda(adicional.costo());

            entidadesAdicionales.add(new CotizacionAdicional(
                    idCotizacion,
                    adicional.idAdicional(),
                    1,
                    costo
            ));

            respuestasAdicionales.add(new CotizacionAdicionalResponse(
                    adicional.idAdicional(),
                    adicional.nombre(),
                    1,
                    costo,
                    costo
            ));

            costoAdicionalTotal =
                    costoAdicionalTotal.add(costo);
        }

        BigDecimal total = moneda(
                subtotalAlojamiento
                        .add(tarifaLimpieza)
                        .add(costoServicio)
                        .add(costoAdicionalTotal)
                        .subtract(montoDescuento)
        );

        OffsetDateTime fechaCotizacion =
                OffsetDateTime.now(ZoneOffset.UTC);

        Cotizacion cotizacion = new Cotizacion(
                idCotizacion,
                publicacion.idPublicacion(),
                configuracion.idConfiguracion(),
                solicitud.fechaEntrada(),
                solicitud.fechaSalida(),
                solicitud.cantidadHuespedes(),
                precioPorNoche,
                tarifaLimpieza,
                fechaCotizacion
        );

        cotizacionRepository.save(cotizacion);

        if (!entidadesAdicionales.isEmpty()) {
            adicionalRepository.saveAll(entidadesAdicionales);
        }

        return new CotizacionResponse(
                idCotizacion,
                publicacion.idPublicacion(),
                configuracion.idConfiguracion(),
                solicitud.fechaEntrada(),
                solicitud.fechaSalida(),
                noches,
                solicitud.cantidadHuespedes(),
                precioPorNoche,
                subtotalAlojamiento,
                tarifaLimpieza,
                configuracion.porcentajeServicio(),
                costoServicio,
                configuracion.porcentajeDescuento(),
                montoDescuento,
                respuestasAdicionales,
                costoAdicionalTotal,
                total,
                fechaCotizacion
        );
    }

    private List<ServicioAdicionalInfo> validarAdicionales(
            UUID idPublicacion,
            List<UUID> idsAdicionales) {

        if (idsAdicionales == null || idsAdicionales.isEmpty()) {
            return List.of();
        }

        Set<UUID> identificadores = new HashSet<>();

        for (UUID idAdicional : idsAdicionales) {
            if (idAdicional == null) {
                throw new IllegalArgumentException(
                        "El identificador de cada servicio adicional es obligatorio."
                );
            }

            if (!identificadores.add(idAdicional)) {
                throw new IllegalArgumentException(
                        "No puedes incluir el mismo servicio adicional más de una vez."
                );
            }
        }

        Map<UUID, ServicioAdicionalInfo> disponibles =
                servicioAdicionalQuery
                        .consultarActivosPorPublicacion(idPublicacion)
                        .stream()
                        .collect(Collectors.toMap(
                                ServicioAdicionalInfo::idAdicional,
                                Function.identity()
                        ));

        List<ServicioAdicionalInfo> seleccionados = new ArrayList<>();

        for (UUID idAdicional : idsAdicionales) {
            ServicioAdicionalInfo adicional =
                    disponibles.get(idAdicional);

            if (adicional == null) {
                throw new IllegalArgumentException(
                        "Uno de los servicios no está activo o no pertenece a esta publicación."
                );
            }

            seleccionados.add(adicional);
        }

        return seleccionados;
    }

    private void validarSolicitud(CrearCotizacionRequest solicitud) {
        if (solicitud == null
                || solicitud.idPublicacion() == null
                || solicitud.fechaEntrada() == null
                || solicitud.fechaSalida() == null) {
            throw new IllegalArgumentException(
                    "Debes indicar publicación y fechas para calcular la cotización."
            );
        }

        if (!solicitud.fechaSalida().isAfter(solicitud.fechaEntrada())) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la fecha de entrada."
            );
        }

        if (solicitud.cantidadHuespedes() < 1) {
            throw new IllegalArgumentException(
                    "Debe indicarse al menos un huésped."
            );
        }
    }

    private BigDecimal calcularPorcentaje(
            BigDecimal base,
            BigDecimal porcentaje) {
        return moneda(
                base.multiply(porcentaje)
                        .divide(CIEN, 2, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal moneda(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}