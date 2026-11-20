package co.edu.unbosque.marketplace.alojamiento.contracts;

public record ActualizarAlojamientoParaPublicacion(
        String ubicacion,
        String ciudad,
        String tipoAlojamiento,
        int capacidad,
        Integer habitaciones,
        Integer camas,
        Integer banos
) {

    public ActualizarAlojamientoParaPublicacion(
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad) {
        this(ubicacion, ciudad, tipoAlojamiento, capacidad, null, null, null);
    }
}
