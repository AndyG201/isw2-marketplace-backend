package co.edu.unbosque.marketplace.cotizacion.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaQuery;
import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;
import co.edu.unbosque.marketplace.cotizacion.infrastructure.persistence.CotizacionRepository;

@Service
public class CotizacionReservaQueryService
        implements CotizacionReservaQuery {

    private final CotizacionRepository cotizacionRepository;

    public CotizacionReservaQueryService(
            CotizacionRepository cotizacionRepository) {

        this.cotizacionRepository = cotizacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionReservaInfo> consultarPorPublicacion(
            UUID idPublicacion) {

        if (idPublicacion == null) {
            return List.of();
        }

        return cotizacionRepository
                .findByIdPublicacion(idPublicacion)
                .stream()
                .map(this::convertirInformacion)
                .toList();
    }

    private CotizacionReservaInfo convertirInformacion(
            Cotizacion cotizacion) {

        return new CotizacionReservaInfo(
                cotizacion.getIdCotizacion(),
                cotizacion.getIdPublicacion(),
                cotizacion.getFechaEntrada(),
                cotizacion.getFechaSalida()
        );
    }
}
