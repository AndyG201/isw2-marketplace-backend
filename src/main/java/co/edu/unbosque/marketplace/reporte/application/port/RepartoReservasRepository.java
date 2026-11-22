package co.edu.unbosque.marketplace.reporte.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.reporte.domain.PagoParaReparto;

/** Pagos aprobados con los datos de la cotización y las devoluciones, listos para repartirlos. */
public interface RepartoReservasRepository {

    /** Todos los pagos aprobados de la plataforma. */
    List<PagoParaReparto> pagosAprobados();

    /** Los pagos aprobados de las reservas de los alojamientos de un anfitrión. */
    List<PagoParaReparto> pagosAprobadosDelAnfitrion(UUID idAnfitrion);
}
