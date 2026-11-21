package co.edu.unbosque.marketplace.alojamiento.contracts;

public record ActualizarAlojamientoParaPublicacion(
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
    public ActualizarAlojamientoParaPublicacion(
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            Integer habitaciones,
            Integer camas,
            Integer banos,
            String propietarioInmueble) {
        this(ubicacion, ciudad, tipoAlojamiento, capacidad, habitaciones, camas, banos, propietarioInmueble,
                null, null);
    }
    public ActualizarAlojamientoParaPublicacion(
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            Integer habitaciones,
            Integer camas,
            Integer banos) {
        this(ubicacion, ciudad, tipoAlojamiento, capacidad, habitaciones, camas, banos, null, null, null);
    }
}
