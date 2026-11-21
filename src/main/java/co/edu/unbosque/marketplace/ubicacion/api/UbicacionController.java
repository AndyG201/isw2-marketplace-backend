package co.edu.unbosque.marketplace.ubicacion.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.ubicacion.application.UbicacionService;
import co.edu.unbosque.marketplace.ubicacion.application.dto.CiudadResponse;
import co.edu.unbosque.marketplace.ubicacion.application.dto.PaisResponse;

@RestController
@RequestMapping("/autenticacion/ubicaciones")
public class UbicacionController {
    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping("/paises")
    public List<PaisResponse> listarPaises() {
        return ubicacionService.listarPaises();
    }

    @GetMapping("/paises/{idPais}/ciudades")
    public List<CiudadResponse> listarCiudades(@PathVariable UUID idPais) {
        return ubicacionService.listarCiudades(idPais);
    }
}
