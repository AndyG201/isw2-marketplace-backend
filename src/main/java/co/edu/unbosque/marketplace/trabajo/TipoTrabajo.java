package co.edu.unbosque.marketplace.trabajo;

public enum TipoTrabajo {
    CONCILIAR_PAGOS(10),
    LIBERAR_RETENCIONES_APROBACION(20),
    EXPIRAR_RETENCIONES(21),
    EXPORTAR_REPORTE(100);

    private final int prioridad;

    TipoTrabajo(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getPrioridad() {
        return prioridad;
    }
}