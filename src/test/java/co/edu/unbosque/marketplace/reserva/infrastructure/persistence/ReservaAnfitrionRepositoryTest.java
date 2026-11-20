package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

@ExtendWith(MockitoExtension.class)
class ReservaAnfitrionRepositoryTest {

    private static final UUID ID_ANFITRION = UUID.randomUUID();
    private static final UUID ID_RESERVA = UUID.randomUUID();
    private static final OffsetDateTime LIMITE = OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private ResultSet fila;

    @InjectMocks
    private ReservaAnfitrionRepository repositorio;

    @Test
    @SuppressWarnings("unchecked")
    void leeElPlazoDeRespuestaDeCadaReserva() throws SQLException {
        ArgumentCaptor<String> consulta = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper<ReservaAnfitrionDatos>> mapeador = ArgumentCaptor.forClass(RowMapper.class);
        when(jdbc.query(consulta.capture(), mapeador.capture(), eq(ID_ANFITRION))).thenReturn(List.of());

        repositorio.consultarPorAnfitrion(ID_ANFITRION);

        assertTrue(consulta.getValue().contains("r.retencion_hasta AS \"retencionHasta\""));
        prepararFila();
        ReservaAnfitrionDatos datos = mapeador.getValue().mapRow(fila, 0);
        assertEquals(LIMITE, datos.retencionHasta());
        assertEquals("PENDIENTE_APROBACION", datos.estado());
        assertEquals(new BigDecimal("820000"), datos.montoTotal());
    }

    @Test
    void consultaUnaReservaDelAnfitrion() {
        when(jdbc.query(anyString(), any(RowMapper.class), eq(ID_ANFITRION), eq(ID_RESERVA)))
                .thenReturn(List.of());

        assertTrue(repositorio.consultarReservaPorAnfitrion(ID_ANFITRION, ID_RESERVA).isEmpty());
        verify(jdbc).query(anyString(), any(RowMapper.class), eq(ID_ANFITRION), eq(ID_RESERVA));
    }

    private void prepararFila() throws SQLException {
        when(fila.getObject("idReserva", UUID.class)).thenReturn(ID_RESERVA);
        when(fila.getObject("idAlojamiento", UUID.class)).thenReturn(UUID.randomUUID());
        when(fila.getString("alojamiento")).thenReturn("Cabaña");
        when(fila.getObject("idHuesped", UUID.class)).thenReturn(UUID.randomUUID());
        when(fila.getString("huesped")).thenReturn("Hugo Pérez");
        when(fila.getObject("fechaEntrada", LocalDate.class)).thenReturn(LocalDate.of(2026, 12, 10));
        when(fila.getObject("fechaSalida", LocalDate.class)).thenReturn(LocalDate.of(2026, 12, 12));
        when(fila.getBigDecimal("montoTotal")).thenReturn(new BigDecimal("820000"));
        when(fila.getString("estado")).thenReturn("PENDIENTE_APROBACION");
        when(fila.getObject("retencionHasta", OffsetDateTime.class)).thenReturn(LIMITE);
    }
}
