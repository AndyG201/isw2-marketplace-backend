package co.edu.unbosque.marketplace.reporte.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExportacionReporteRepository {

    void crear(UUID id, UUID administrador, String formato, OffsetDateTime fecha);

    Optional<ExportacionReporteInfo> buscar(UUID id, UUID administrador);

    Optional<UUID> buscarAdministrador(UUID id);

    boolean iniciar(UUID id);

    void completar(UUID id, OffsetDateTime corte, OffsetDateTime generacion, String ruta);

    void fallar(UUID id);

    int recuperarProcesosAbandonados(OffsetDateTime anterioresA);

    List<UUID> listarIdsSolicitados(int limite);
}
