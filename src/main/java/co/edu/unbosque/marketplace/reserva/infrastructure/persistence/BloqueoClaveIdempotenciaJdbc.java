package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.application.port.BloqueoClaveIdempotencia;

@Repository
public class BloqueoClaveIdempotenciaJdbc implements BloqueoClaveIdempotencia {

    private final JdbcTemplate jdbc;

    public BloqueoClaveIdempotenciaJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void bloquear(String ambito, String clave) {
        jdbc.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                resultado -> {
                    resultado.next();
                    return null;
                },
                ambito + ":" + clave);
    }
}
