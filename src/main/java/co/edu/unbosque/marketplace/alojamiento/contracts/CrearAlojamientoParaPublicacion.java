
package co.edu.unbosque.marketplace.alojamiento.contracts;

import java.util.UUID;

public record CrearAlojamientoParaPublicacion(
        UUID idAnfitrion,
        String ubicacion,
        String ciudad,
        String tipoAlojamiento,
        int capacidad
) {
}
