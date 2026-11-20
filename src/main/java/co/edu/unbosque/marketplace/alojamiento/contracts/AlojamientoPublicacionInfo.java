package co.edu.unbosque.marketplace.alojamiento.contracts;

import java.util.UUID;

public record AlojamientoPublicacionInfo(
        UUID idAlojamiento,
        UUID idAnfitrion,
        String ubicacion,
        String ciudad,
        String tipoAlojamiento,
        int capacidad,
        int habitaciones,
        int camas,
        int banos,
        String propietarioInmueble
) {
    public AlojamientoPublicacionInfo(
            UUID idAlojamiento,
            UUID idAnfitrion,
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            int habitaciones,
            int camas,
            int banos) {
        this(idAlojamiento, idAnfitrion, ubicacion, ciudad, tipoAlojamiento, capacidad,
                habitaciones, camas, banos, null);
    }


    public AlojamientoPublicacionInfo(
            UUID idAlojamiento,
            UUID idAnfitrion,
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad) {
        this(idAlojamiento, idAnfitrion, ubicacion, ciudad, tipoAlojamiento, capacidad, 1, 1, 1, null);
    }
}
