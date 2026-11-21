package co.edu.unbosque.marketplace.trabajo;

import java.util.Optional;

public interface ColaTrabajos {

    void encolar(TipoTrabajo tipo, String identificador);

    Optional<Trabajo> tomarSiguiente();

    record Trabajo(TipoTrabajo tipo, String identificador) {
    }
}
