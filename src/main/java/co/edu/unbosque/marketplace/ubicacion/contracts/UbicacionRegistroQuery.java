package co.edu.unbosque.marketplace.ubicacion.contracts;

import java.util.Optional;
import java.util.UUID;

public interface UbicacionRegistroQuery {
    boolean existeCiudad(UUID idCiudad);

    Optional<CiudadUbicacionInfo> consultarCiudad(UUID idCiudad);
}
