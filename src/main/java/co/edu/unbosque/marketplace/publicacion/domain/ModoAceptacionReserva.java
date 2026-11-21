package co.edu.unbosque.marketplace.publicacion.domain;

import java.util.Locale;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;

public enum ModoAceptacionReserva {

    INMEDIATA,
    APROBACION;

    public static ModoAceptacionReserva desde(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException(
                    "El modo de aceptación es obligatorio."
            );
        }

        try {

            return valueOf(
                    valor.trim().toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {

            throw new DatosInvalidosException(
                    "El modo de aceptación debe ser INMEDIATA o APROBACION."
            );
        }
    }
}
