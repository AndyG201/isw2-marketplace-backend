package co.edu.unbosque.marketplace.alojamiento.contracts;

public record ActualizarAlojamientoParaPublicacion(
        String ubicacion,
        String ciudad,
        String tipoAlojamiento,
        int capacidad,
        Integer habitaciones,
        Integer camas,
        Integer banos,
        String propietarioInmueble
) {
    public ActualizarAlojamientoParaPublicacion(
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            Integer habitaciones,
            Integer camas,
            Integer banos) {
        this(ubicacion, ciudad, tipoAlojamiento, capacidad, habitaciones, camas, banos, null);
    }
}
