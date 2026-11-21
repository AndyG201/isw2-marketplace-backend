package co.edu.unbosque.marketplace.reporte.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

class ProgramadorExportacionesTest {

    @Test
    void recuperaLasExportacionesAbandonadasYEncolaLasSolicitadas() {
        ColaTrabajos cola = mock(ColaTrabajos.class);
        ExportacionReporteRepository exportaciones = mock(ExportacionReporteRepository.class);
        UUID idExportacion = UUID.randomUUID();
        when(exportaciones.listarIdsSolicitados(100)).thenReturn(List.of(idExportacion));

        new ProgramadorExportaciones(cola, exportaciones).recuperarExportacionesSolicitadas();

        verify(exportaciones).recuperarProcesosAbandonados(any());
        verify(cola).encolar(TipoTrabajo.EXPORTAR_REPORTE, idExportacion.toString());
    }
}
