package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReservaAfectadaQuery {

    List<ReservaAfectadaInfo> consultarPorCotizaciones(
            Collection<UUID> idsCotizaciones
    );
}
