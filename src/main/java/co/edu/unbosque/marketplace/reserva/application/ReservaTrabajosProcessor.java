package co.edu.unbosque.marketplace.reserva.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.trabajo.ProcesadorTrabajo;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Component
@Profile("worker")
public class ReservaTrabajosProcessor implements ProcesadorTrabajo {

    private final ReservaPagoConciliacionWorker conciliador;
    private final ReservaAprobacionRetencionScheduler aprobacion;
    private final ReservaRetencionScheduler retenciones;

    public ReservaTrabajosProcessor(
            ReservaPagoConciliacionWorker conciliador,
            ReservaAprobacionRetencionScheduler aprobacion,
            ReservaRetencionScheduler retenciones) {
        this.conciliador = conciliador;
        this.aprobacion = aprobacion;
        this.retenciones = retenciones;
    }

    @Override
    public boolean soporta(TipoTrabajo tipo) {
        return tipo == TipoTrabajo.CONCILIAR_PAGOS
                || tipo == TipoTrabajo.LIBERAR_RETENCIONES_APROBACION
                || tipo == TipoTrabajo.EXPIRAR_RETENCIONES;
    }

    @Override
    public void procesar(TipoTrabajo tipo, String identificador) {
        switch (tipo) {
            case CONCILIAR_PAGOS -> conciliador.conciliarPendientes();
            case LIBERAR_RETENCIONES_APROBACION ->
                    aprobacion.rechazarSolicitudesVencidas();
            case EXPIRAR_RETENCIONES -> retenciones.expirarReservasSinPago();
            default -> throw new FallaInternaException(
                    "El procesador de reservas no admite el trabajo " + tipo);
        }
    }
}