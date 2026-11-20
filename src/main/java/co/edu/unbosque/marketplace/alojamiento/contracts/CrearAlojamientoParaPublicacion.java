package co.edu.unbosque.marketplace.alojamiento.contracts;

import java.util.UUID;

public record CrearAlojamientoParaPublicacion(
        UUID idAnfitrion,
        String ubicacion,
        String ciudad,
        String tipoAlojamiento,
        int capacidad,
        Integer habitaciones,
        Integer camas,
        Integer banos,
        String propietarioInmueble,
        String pais,
        String codigoPais
) {
    public CrearAlojamientoParaPublicacion(
            UUID idAnfitrion,
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            Integer habitaciones,
            Integer camas,
            Integer banos,
            String propietarioInmueble) {
        this(idAnfitrion, ubicacion, ciudad, tipoAlojamiento, capacidad, habitaciones, camas, banos,
                propietarioInmueble, null, null);
    }
    public CrearAlojamientoParaPublicacion(
            UUID idAnfitrion,
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            Integer habitaciones,
            Integer camas,
            Integer banos) {
        this(idAnfitrion, ubicacion, ciudad, tipoAlojamiento, capacidad, habitaciones, camas, banos, null, null, null);
    }


    public CrearAlojamientoParaPublicacion(
            UUID idAnfitrion,
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad) {
        this(idAnfitrion, ubicacion, ciudad, tipoAlojamiento, capacidad, null, null, null, null, null, null);
    }
}
