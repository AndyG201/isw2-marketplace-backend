package co.edu.unbosque.marketplace.cancelacion.domain;

import java.util.Locale;

public enum TipoPoliticaCancelacion {

    FLEXIBLE,
    MODERADA,
    ESTRICTA;

    public static boolean esPermitida(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        try {

            valueOf(nombre.trim().toUpperCase(Locale.ROOT));

            return true;

        } catch (IllegalArgumentException e) {

            return false;
        }
    }
}
