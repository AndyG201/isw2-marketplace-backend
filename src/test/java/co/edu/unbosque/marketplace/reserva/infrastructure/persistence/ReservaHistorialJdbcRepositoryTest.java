package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import co.edu.unbosque.marketplace.reserva.application.port.ReservaHistorialDatos;

@ExtendWith(MockitoExtension.class)
class ReservaHistorialJdbcRepositoryTest {

    private static final UUID ID_HUESPED = UUID.randomUUID();

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private ResultSet fila;

    @InjectMocks
    private ReservaHistorialJdbcRepository repositorio;

    @Test
    @SuppressWarnings("unchecked")
    void leeSiLaReservaYaTieneResena() throws SQLException {
        ArgumentCaptor<String> consulta = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper<ReservaHistorialDatos>> mapeador = ArgumentCaptor.forClass(RowMapper.class);
        when(jdbc.query(consulta.capture(), mapeador.capture(), eq(ID_HUESPED))).thenReturn(List.of());

        repositorio.consultarPorHuesped(ID_HUESPED);

        assertTrue(consulta.getValue().contains("AS \"resenada\""));
        when(fila.getObject("idReserva", UUID.class)).thenReturn(UUID.randomUUID());
        when(fila.getObject("idAlojamiento", UUID.class)).thenReturn(UUID.randomUUID());
        when(fila.getString("alojamiento")).thenReturn("Loft");
        when(fila.getObject("fechaEntrada", LocalDate.class)).thenReturn(LocalDate.of(2026, 12, 1));
        when(fila.getObject("fechaSalida", LocalDate.class)).thenReturn(LocalDate.of(2026, 12, 4));
        when(fila.getBigDecimal("montoTotal")).thenReturn(new BigDecimal("624000"));
        when(fila.getString("estado")).thenReturn("COMPLETADA");
        when(fila.getObject("fechaSolicitud", OffsetDateTime.class)).thenReturn(null);
        when(fila.getObject("retencionHasta", OffsetDateTime.class)).thenReturn(null);
        when(fila.getBoolean("resenada")).thenReturn(true);

        assertTrue(mapeador.getValue().mapRow(fila, 0).resenada());
    }
}
