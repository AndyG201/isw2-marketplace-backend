package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;

public interface CatalogoUbicacionesExterno {

    List<PaisExterno> paises();

    List<String> ciudades(String nombrePais);

    record PaisExterno(String nombre, String codigoIso2) {
    }
}
