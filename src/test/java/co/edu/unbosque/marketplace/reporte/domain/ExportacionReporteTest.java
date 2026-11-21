package co.edu.unbosque.marketplace.reporte.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ExportacionReporteTest {

    @Test
    void constructorVacioDebeCrearObjeto() {
        ExportacionReporte reporte = new ExportacionReporte();

        assertNotNull(reporte);
    }

    @Test
    void atributosDebenExistirEnLaClase() {
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("idExportacion"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("idAdministrador"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("tipoReporte"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("formato"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("filtros"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("estado"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("fechaSolicitud"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("fechaCorteDatos"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("fechaGeneracion"));
        assertDoesNotThrow(() -> ExportacionReporte.class.getDeclaredField("rutaArchivo"));
    }

    @Test
    void atributosDebenTenerLosTiposCorrectos() throws NoSuchFieldException {
        assertEquals(UUID.class, ExportacionReporte.class.getDeclaredField("idExportacion").getType());
        assertEquals(UUID.class, ExportacionReporte.class.getDeclaredField("idAdministrador").getType());
        assertEquals(String.class, ExportacionReporte.class.getDeclaredField("tipoReporte").getType());
        assertEquals(String.class, ExportacionReporte.class.getDeclaredField("formato").getType());
        assertEquals(String.class, ExportacionReporte.class.getDeclaredField("filtros").getType());
        assertEquals(String.class, ExportacionReporte.class.getDeclaredField("estado").getType());
        assertEquals(OffsetDateTime.class, ExportacionReporte.class.getDeclaredField("fechaSolicitud").getType());
        assertEquals(OffsetDateTime.class, ExportacionReporte.class.getDeclaredField("fechaCorteDatos").getType());
        assertEquals(OffsetDateTime.class, ExportacionReporte.class.getDeclaredField("fechaGeneracion").getType());
        assertEquals(String.class, ExportacionReporte.class.getDeclaredField("rutaArchivo").getType());
    }

    @Test
    void constructorDebeInicializarAtributosEnNulo() throws Exception {
        ExportacionReporte reporte = new ExportacionReporte();

        for (Field campo : ExportacionReporte.class.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(campo.getModifiers())) {
                campo.setAccessible(true);
                assertNull(campo.get(reporte));
            }
        }
    }
}