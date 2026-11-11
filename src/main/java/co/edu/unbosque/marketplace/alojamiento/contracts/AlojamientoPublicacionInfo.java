
package co.edu.unbosque.marketplace.alojamiento.contracts;

import java.util.UUID;

public record AlojamientoPublicacionInfo(
        UUID idAlojamiento,
        UUID idAnfitrion,
        String ubicacion,
        String ciudad,
        String tipoAlojamiento,
        int capacidad
) {
}
