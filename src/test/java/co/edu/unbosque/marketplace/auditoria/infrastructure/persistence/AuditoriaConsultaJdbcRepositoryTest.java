package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.dao.DataAccessResourceFailureException;

import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

class AuditoriaConsultaJdbcRepositoryTest {

    private static final String URL = "jdbc:postgresql://auditoria-test/eventos";
    private static final String USER = "auditoria_reader";
    private static final String PASSWORD = "clave-test";

    @Test
    void consultarFiltraOrdenaPaginaYMapeaElEvento() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.parse("2026-11-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-11-20T23:59:59Z");
        OffsetDateTime eventDate = OffsetDateTime.parse("2026-11-15T13:30:00Z");
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id_evento", UUID.class)).thenReturn(eventId);
        when(resultSet.getObject("id_usuario", UUID.class)).thenReturn(userId);
        when(resultSet.getObject("fecha_evento", OffsetDateTime.class)).thenReturn(eventDate);
        when(resultSet.getString("accion")).thenReturn("PUBLICACION_APROBADA");
        when(resultSet.getString("resultado")).thenReturn("EXITOSO");
        when(resultSet.getString("entidad_afectada")).thenReturn("PUBLICACION");
        when(resultSet.getObject("id_entidad_afectada", UUID.class)).thenReturn(entityId);
        when(resultSet.getString("detalle_json")).thenReturn("{\"estado\":\"ACTIVA\"}");
        when(resultSet.getString("id_correlacion")).thenReturn("corr-7");
        FiltrosAuditoria filtros = new FiltrosAuditoria(
                userId, from, to, "PUBLICACION_APROBADA", "EXITOSO", "PUBLICACION");

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(eq(URL), any(Properties.class))).thenReturn(connection);
            AuditoriaConsultaJdbcRepository repository = new AuditoriaConsultaJdbcRepository(URL, USER, PASSWORD);
            var rows = repository.consultar(filtros, 25, 50L);

            assertEquals(1, rows.size());
            EventoAuditoria event = rows.get(0);
            assertEquals(eventId, event.idEvento());
            assertEquals(userId, event.idUsuario());
            assertEquals(eventDate, event.fechaEvento());
            assertEquals("PUBLICACION_APROBADA", event.accion());
            assertEquals("EXITOSO", event.resultado());
            assertEquals("PUBLICACION", event.entidadAfectada());
            assertEquals(entityId, event.idEntidadAfectada());
            assertEquals("{\"estado\":\"ACTIVA\"}", event.detalleJson());
            assertEquals("corr-7", event.idCorrelacion());

            ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
            verify(connection).prepareStatement(sql.capture());
            assertTrue(sql.getValue().contains("id_usuario = ?"));
            assertTrue(sql.getValue().contains("fecha_evento >= ?"));
            assertTrue(sql.getValue().contains("fecha_evento <= ?"));
            assertTrue(sql.getValue().contains("ORDER BY fecha_evento DESC, id_evento DESC LIMIT ? OFFSET ?"));
            assertFalse(sql.getValue().contains("PUBLICACION_APROBADA"));
            verify(statement).setObject(1, userId);
            verify(statement).setObject(2, from);
            verify(statement).setObject(3, to);
            verify(statement).setString(4, "PUBLICACION_APROBADA");
            verify(statement).setString(5, "EXITOSO");
            verify(statement).setString(6, "PUBLICACION");
            verify(statement).setObject(7, 25);
            verify(statement).setObject(8, 50L);
        }
    }

    @Test
    void contarDevuelveElTotal() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(1);
        when(resultSet.getLong(1)).thenReturn(7L);
        when(resultSet.wasNull()).thenReturn(false);

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(eq(URL), any(Properties.class))).thenReturn(connection);
            AuditoriaConsultaJdbcRepository repository = new AuditoriaConsultaJdbcRepository(URL, USER, PASSWORD);
            assertEquals(7L, repository.contar(new FiltrosAuditoria(null, null, null, null, null, null)));
        }
    }

    @Test
    void buscarDevuelveVacioCuandoNoExisteElEvento() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        UUID eventId = UUID.randomUUID();

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(eq(URL), any(Properties.class))).thenReturn(connection);
            AuditoriaConsultaJdbcRepository repository = new AuditoriaConsultaJdbcRepository(URL, USER, PASSWORD);
            Optional<EventoAuditoria> result = repository.buscar(eventId);
            assertTrue(result.isEmpty());
            verify(statement).setObject(1, eventId);
        }
    }

    @Test
    void rechazaConsultasSiElAlmacenNoEstaConfigurado() {
        AuditoriaConsultaJdbcRepository repository = new AuditoriaConsultaJdbcRepository(" ", USER, PASSWORD);
        FiltrosAuditoria filtros = new FiltrosAuditoria(null, null, null, null, null, null);

        assertThrows(DataAccessResourceFailureException.class, () -> repository.consultar(filtros, 10, 0));
        assertThrows(DataAccessResourceFailureException.class, () -> repository.contar(filtros));
        assertThrows(DataAccessResourceFailureException.class, () -> repository.buscar(UUID.randomUUID()));
    }
}