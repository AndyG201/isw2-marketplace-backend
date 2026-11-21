package co.edu.unbosque.marketplace.publicacion.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.publicacion.api.response.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;
import co.edu.unbosque.marketplace.publicacion.infrastructure.cache.PublicacionBusquedaCache;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionBusquedaRepository;

@Service
public class PublicacionBusquedaService {

    private static final int TAMANO_MAXIMO = 100;

    private final PublicacionBusquedaRepository repository;
    private final PublicacionBusquedaCache cache;

    public PublicacionBusquedaService(
            PublicacionBusquedaRepository repository,
            PublicacionBusquedaCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    public PaginaPublicacionesResponse buscar(
            PublicacionBusquedaCriteria entrada) {
        PublicacionBusquedaCriteria criteria =
                normalizarYValidar(entrada);

        String version = cache.versionActual();
        String key = version == null
                ? null
                : cache.clave(version, criteria);

        if (key != null) {
            PaginaPublicacionesResponse encontrada = cache.leer(key);
            if (encontrada != null) {
                return encontrada;
            }
        }

        PaginaPublicacionesResponse resultado =
                repository.buscar(criteria);

        if (key != null) {
            cache.guardar(key, resultado);
        }

        return resultado;
    }

    static PublicacionBusquedaCriteria normalizarYValidar(
            PublicacionBusquedaCriteria entrada) {
        Objects.requireNonNull(
                entrada,
                "Los filtros de búsqueda son obligatorios."
        );

        if ((entrada.fechaInicio() == null)
                != (entrada.fechaFin() == null)) {
            throw new IllegalArgumentException(
                    "Debes indicar las fechas de llegada y salida juntas."
            );
        }

        if (entrada.fechaInicio() != null
                && !entrada.fechaFin().isAfter(entrada.fechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la fecha de llegada."
            );
        }

        if (entrada.capacidad() != null && entrada.capacidad() < 1) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero."
            );
        }

        validarPrecioNoNegativo(
                entrada.precioMinimo(),
                "El precio mínimo"
        );
        validarPrecioNoNegativo(
                entrada.precioMaximo(),
                "El precio máximo"
        );

        if (entrada.precioMinimo() != null
                && entrada.precioMaximo() != null
                && entrada.precioMinimo()
                        .compareTo(entrada.precioMaximo()) > 0) {
            throw new IllegalArgumentException(
                    "El precio mínimo no puede superar el precio máximo."
            );
        }

        if (entrada.pagina() < 0) {
            throw new IllegalArgumentException(
                    "La página debe ser cero o un número positivo."
            );
        }

        if (entrada.tamano() < 1
                || entrada.tamano() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y "
                            + TAMANO_MAXIMO + "."
            );
        }

        String ciudad = normalizarTexto(entrada.ciudad());
        String tipo = normalizarTexto(entrada.tipoAlojamiento());

        List<UUID> servicios = entrada.servicios().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        return new PublicacionBusquedaCriteria(
                ciudad == null
                        ? null
                        : ciudad.toLowerCase(Locale.ROOT),
                entrada.fechaInicio(),
                entrada.fechaFin(),
                entrada.capacidad(),
                tipo == null
                        ? null
                        : tipo.toLowerCase(Locale.ROOT),
                entrada.precioMinimo(),
                entrada.precioMaximo(),
                servicios,
                entrada.pagina(),
                entrada.tamano()
        );
    }

    private static String normalizarTexto (String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private static void validarPrecioNoNegativo(
            BigDecimal precio,
            String nombre) {
        if (precio != null && precio.signum() < 0) {
            throw new IllegalArgumentException(
                    nombre + " no puede ser negativo."
            );
        }
    }
}