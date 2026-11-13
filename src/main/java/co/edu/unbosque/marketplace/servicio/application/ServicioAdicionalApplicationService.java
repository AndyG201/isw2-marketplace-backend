package co.edu.unbosque.marketplace.servicio.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalCommand;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalData;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;
import co.edu.unbosque.marketplace.servicio.domain.ServicioAdicional;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioAdicionalRepository;

@Service
public class ServicioAdicionalApplicationService
        implements ServicioAdicionalCommand, ServicioAdicionalQuery {

    private final ServicioAdicionalRepository repository;

    public ServicioAdicionalApplicationService(
            ServicioAdicionalRepository repository) {

        this.repository = repository;
    }

    @Override
    @Transactional
    public List<ServicioAdicionalInfo> reemplazarActivosPorPublicacion(
            UUID idPublicacion,
            List<ServicioAdicionalData> servicios) {

        validarDatos(idPublicacion, servicios);

        List<ServicioAdicional> actuales =
                repository.findByIdPublicacionAndActivoTrueOrderByNombreAsc(
                        idPublicacion
                );

        // Se conservan los registros históricos.
        actuales.forEach(servicio -> servicio.setActivo(false));

        if (!actuales.isEmpty()) {
            repository.saveAll(actuales);
        }

        if (servicios.isEmpty()) {
            return List.of();
        }

        List<ServicioAdicional> nuevos = new ArrayList<>();

        for (ServicioAdicionalData data : servicios) {

            ServicioAdicional adicional = new ServicioAdicional(
                    UUID.randomUUID(),
                    idPublicacion,
                    data.nombre().trim(),
                    normalizarDescripcion(data.descripcion()),
                    data.costo(),
                    true
            );

            nuevos.add(adicional);
        }

        return repository.saveAll(nuevos)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioAdicionalInfo> consultarActivosPorPublicacion(
            UUID idPublicacion) {

        if (idPublicacion == null) {
            return List.of();
        }

        return repository
                .findByIdPublicacionAndActivoTrueOrderByNombreAsc(
                        idPublicacion
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    private void validarDatos(
            UUID idPublicacion,
            List<ServicioAdicionalData> servicios) {

        if (idPublicacion == null || servicios == null) {
            throw new IllegalArgumentException(
                    "La configuración de servicios no es válida."
            );
        }

        Set<String> nombres = new HashSet<>();

        for (ServicioAdicionalData servicio : servicios) {

            if (servicio == null
                    || servicio.nombre() == null
                    || servicio.nombre().isBlank()
                    || servicio.nombre().trim().length() > 100) {

                throw new IllegalArgumentException(
                        "Cada servicio debe tener un nombre válido de máximo 100 caracteres."
                );
            }

            if (servicio.costo() == null
                    || servicio.costo().signum() < 0) {

                throw new IllegalArgumentException(
                        "El costo de cada servicio no puede ser negativo."
                );
            }

            String nombreNormalizado =
                    servicio.nombre().trim().toLowerCase(Locale.ROOT);

            if (!nombres.add(nombreNormalizado)) {

                throw new IllegalArgumentException(
                        "No puedes registrar servicios con nombres duplicados."
                );
            }
        }
    }

    private String normalizarDescripcion(String descripcion) {

        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        return descripcion.trim();
    }

    private ServicioAdicionalInfo convertir(
            ServicioAdicional servicio) {

        return new ServicioAdicionalInfo(
                servicio.getIdAdicional(),
                servicio.getIdPublicacion(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getCosto(),
                servicio.isActivo()
        );
    }
}
