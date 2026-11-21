package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;

@Service
public class CuentasSinVerificarService {

    private static final Logger LOG = LoggerFactory.getLogger(CuentasSinVerificarService.class);

    private final UsuarioRepository usuarioRepository;
    private final long horasLimite;

    public CuentasSinVerificarService(
            UsuarioRepository usuarioRepository,
            @Value("${app.usuario.pendiente.horas-limite:24}") long horasLimite) {
        if (horasLimite <= 0) {
            throw new IllegalArgumentException("Las horas límite deben ser mayores que cero.");
        }
        this.usuarioRepository = usuarioRepository;
        this.horasLimite = horasLimite;
    }

    @Transactional
    public int eliminarVencidas(OffsetDateTime ahora) {
        OffsetDateTime limite = ahora.minusHours(horasLimite);
        int eliminadas = usuarioRepository.eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, limite);
        if (eliminadas > 0) {
            LOG.info("Se eliminaron {} cuentas sin verificar registradas antes de {}.", eliminadas, limite);
        }
        return eliminadas;
    }
}
