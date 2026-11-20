package co.edu.unbosque.marketplace.trabajo;

public interface ProcesadorTrabajo {

    boolean soporta(TipoTrabajo tipo);

    void procesar(TipoTrabajo tipo, String identificador);
}