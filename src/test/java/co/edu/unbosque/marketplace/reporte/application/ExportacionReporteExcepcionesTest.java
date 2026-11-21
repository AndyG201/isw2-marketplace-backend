package co.edu.unbosque.marketplace.reporte.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaAdministracionCommand;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.notificacion.contracts.NotificacionUsuarioCommand;
import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

class ExportacionReporteExcepcionesTest {

    private final ExportacionReporteRepository repositorio = mock(ExportacionReporteRepository.class);
    private final ObjectMapper mapper = mock(ObjectMapper.class);
    private final UUID administrador = UUID.randomUUID();

    private ExportacionReporteService servicio() {
        return new ExportacionReporteService(
                repositorio, mock(AuditoriaAdministracionCommand.class), mock(ColaTrabajos.class), mapper);
    }

    @Test
    void solicitarExigeAdministradorYFormato() {
        assertThrows(DatosInvalidosException.class, () -> servicio().solicitar(null, "PDF"));
        assertThrows(DatosInvalidosException.class, () -> servicio().solicitar(administrador, null));
    }

    @Test
    void solicitarSoloAceptaPdfOExcel() {
        assertThrows(DatosInvalidosException.class, () -> servicio().solicitar(administrador, "CSV"));
    }

    @Test
    void solicitarFallaSiNoSePuedeSerializarElEventoDeAuditoria() throws Exception {
        when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("no") { });

        assertThrows(FallaInternaException.class, () -> servicio().solicitar(administrador, "pdf"));
    }

    // ---------- ExportacionReporteWorker ----------

    private ExportacionReporteWorker worker(ExportacionReporteService servicio) {
        return new ExportacionReporteWorker(
                repositorio, mock(ReporteArchivoGenerator.class), servicio, mock(NotificacionUsuarioCommand.class));
    }

    @Test
    void elWorkerNoAdmiteOtrosTiposDeTrabajo() {
        assertThrows(FallaInternaException.class,
                () -> worker(mock(ExportacionReporteService.class)).procesar(TipoTrabajo.COPIAR_AUDITORIA, "x"));
    }

    @Test
    void siLaExportacionYaNoExisteSeRegistraComoFallida() {
        UUID idExportacion = UUID.randomUUID();
        ExportacionReporteService servicioMock = mock(ExportacionReporteService.class);
        when(repositorio.buscarAdministrador(idExportacion)).thenReturn(Optional.of(administrador));
        when(repositorio.iniciar(idExportacion)).thenReturn(true);
        when(repositorio.buscar(idExportacion, administrador)).thenReturn(Optional.empty());

        worker(servicioMock).procesar(idExportacion);

        verify(servicioMock).fallar(eq(idExportacion), eq(administrador), any(), anyString());
    }
}
