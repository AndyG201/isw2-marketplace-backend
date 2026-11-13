package co.edu.unbosque.marketplace.publicacion.domain;

import java.util.Locale;

public enum ModoAceptacionReserva {

    INMEDIATA,
    APROBACION;

    public static ModoAceptacionReserva desde(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El modo de aceptación es obligatorio."
            );
        }

        try {

            return valueOf(
                    valor.trim().toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "El modo de aceptación debe ser INMEDIATA o APROBACION."
            );
        }
    }
}
