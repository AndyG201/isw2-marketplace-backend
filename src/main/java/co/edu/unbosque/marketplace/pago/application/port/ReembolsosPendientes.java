package co.edu.unbosque.marketplace.pago.application.port;

import java.time.OffsetDateTime;
import java.util.List;

import co.edu.unbosque.marketplace.pago.application.ReembolsoTrabajo;

/**
 * Reclama los reembolsos que el worker debe enviar a Stripe. La implementación debe marcarlos como
 * EN_PROCESO de forma atómica para que dos workers no tomen el mismo reembolso.
 */
public interface ReembolsosPendientes {

    List<ReembolsoTrabajo> reclamar(OffsetDateTime ahora);
}
