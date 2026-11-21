package co.edu.unbosque.marketplace.publicacion.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionBusquedaService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionCatalogoService;
import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

@RestController
@RequestMapping("/publicaciones")
public class PublicacionCatalogoController {

	private final PublicacionCatalogoService catalogoService;
	private final PublicacionBusquedaService busquedaService;

	public PublicacionCatalogoController(PublicacionCatalogoService catalogoService,
			PublicacionBusquedaService busquedaService) {
		this.catalogoService = catalogoService;
		this.busquedaService = busquedaService;
	}

	@GetMapping("/catalogo")
	public ResponseEntity<List<PublicacionResumenResponse>> consultarCatalogo() {

		return ResponseEntity.ok(catalogoService.consultarCatalogo());
	}

	@GetMapping("/busqueda")
	public ResponseEntity<?> buscarPublicaciones(@RequestParam(name = "ciudad", required = false) String ciudad,
			@RequestParam(name = "fechaInicio", required = false) LocalDate fechaInicio,
			@RequestParam(name = "fechaFin", required = false) LocalDate fechaFin,
			@RequestParam(name = "capacidad", required = false) Integer capacidad,
			@RequestParam(name = "tipoAlojamiento", required = false) String tipoAlojamiento,
			@RequestParam(name = "precioMinimo", required = false) BigDecimal precioMinimo,
			@RequestParam(name = "precioMaximo", required = false) BigDecimal precioMaximo,
			@RequestParam(name = "servicios", required = false) List<UUID> servicios,
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			@RequestParam(name = "tamano", defaultValue = "20") int tamano) {

		PaginaPublicacionesResponse resultado = busquedaService
				.buscar(new PublicacionBusquedaCriteria(ciudad, fechaInicio, fechaFin, capacidad, tipoAlojamiento,
						precioMinimo, precioMaximo, servicios, pagina, tamano));

		return ResponseEntity.ok(resultado);
	}
}
