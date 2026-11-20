package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

class PublicacionBusquedaRepositoryTest {

    @Test
    void normalizaLaCiudadSinTildesNiMayusculasYEscapaLosComodines() {
        assertEquals("bogota", PublicacionBusquedaRepository.patronCiudad("  BOGOTÁ "));
        assertEquals("medellin", PublicacionBusquedaRepository.patronCiudad("Medellín"));
        assertEquals("pena\\_\\%", PublicacionBusquedaRepository.patronCiudad("Peña_%"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buscaLaCiudadPorCoincidenciaParcialSinImportarTildes() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class))).thenReturn(0L);
        when(jdbc.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class))).thenReturn(List.of());
        PublicacionBusquedaRepository repositorio = new PublicacionBusquedaRepository(jdbc);

        repositorio.buscar(new PublicacionBusquedaCriteria(
                "bogota", null, null, null, null, null, null, List.of(), 0, 20));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parametros = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForObject(sql.capture(), parametros.capture(), eq(Long.class));
        assertTrue(sql.getValue().contains("translate(lower(a.ciudad), :conTilde, :sinTilde) LIKE :ciudad"));
        assertEquals("%bogota%", parametros.getValue().getValue("ciudad"));
    }
}
