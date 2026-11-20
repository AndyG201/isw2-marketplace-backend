package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

class AlmacenAuditoriaInmutableJdbcTest {

    private static final String URL = "jdbc:postgresql://auditoria-test/eventos";
    private static final String USER = "auditoria_writer";
    private static final String PASSWORD = "clave-test";

    @Test
    void rechazaLaConexionSiNoSeConfiguroUrl() {
        AlmacenAuditoriaInmutableJdbc almacen = new AlmacenAuditoriaInmutableJdbc(" ", USER, PASSWORD);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> almacen.anexar(evento()));

        assertTrue(error.getMessage().contains("APP_AUDITORIA_ALMACEN_URL"));
    }

    @Test
    void insertaEventoConParametrosYJsonb() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(URL, USER, PASSWORD)).thenReturn(connection);
            AlmacenAuditoriaInmutableJdbc almacen = new AlmacenAuditoriaInmutableJdbc(URL, USER, PASSWORD);
            almacen.anexar(evento(eventId, userId, entityId));

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(connection).prepareStatement(sql.capture());
            assertTrue(sql.getValue().contains("INSERT INTO public.evento_auditoria"));
            assertTrue(sql.getValue().contains("CAST(? AS jsonb)"));
            verify(statement).setObject(1, eventId);
            verify(statement).setObject(2, userId);
            verify(statement).setString(4, "PUBLICACION_APROBADA");
            verify(statement).setString(5, "EXITOSO");
            verify(statement).setString(6, "PUBLICACION");
            verify(statement).setObject(7, entityId);
            verify(statement).setString(8, "{\"estado\":\"ACTIVA\"}");
            verify(statement).setString(9, "corr-1");
            verify(statement).executeUpdate();
        }
    }

    @Test
    void insertaNulosEnIdentificadoresOpcionales() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(URL, USER, PASSWORD)).thenReturn(connection);
            new AlmacenAuditoriaInmutableJdbc(URL, USER, PASSWORD).anexar(evento(
                    UUID.randomUUID(), null, null));

            verify(statement).setNull(2, java.sql.Types.OTHER);
            verify(statement).setNull(7, java.sql.Types.OTHER);
            verify(statement).executeUpdate();
        }
    }

    @Test
    void transformaFalloSqlEnErrorDeAlmacen() throws Exception {
        SQLException causa = new SQLException("base no disponible");
        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(URL, USER, PASSWORD)).thenThrow(causa);
            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> new AlmacenAuditoriaInmutableJdbc(URL, USER, PASSWORD).anexar(evento()));
            assertEquals(causa, error.getCause());
        }
    }

    private BandejaSalidaAuditoria evento() {
        return evento(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private BandejaSalidaAuditoria evento(UUID eventId, UUID userId, UUID entityId) {
        BandejaSalidaAuditoria event = new BandejaSalidaAuditoria();
        event.setIdEvento(eventId);
        event.setIdUsuario(userId);
        event.setFechaEvento(OffsetDateTime.parse("2026-11-20T10:15:30Z"));
        event.setAccion("PUBLICACION_APROBADA");
        event.setResultado("EXITOSO");
        event.setEntidadAfectada("PUBLICACION");
        event.setIdEntidadAfectada(entityId);
        event.setDetalle("{\"estado\":\"ACTIVA\"}");
        event.setIdCorrelacion("corr-1");
        return event;
    }
}