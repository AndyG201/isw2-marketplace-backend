package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.usuario.application.port.CorreoPendienteRepository;
import co.edu.unbosque.marketplace.usuario.domain.CorreoPendiente;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

@Service
public class CorreoOutboxService {
    private static final int MAX_ERROR = 1000;
    private final CorreoPendienteRepository repository;
    private final ProteccionDocumento proteccion;

    public CorreoOutboxService(CorreoPendienteRepository repository, ProteccionDocumento proteccion) {
        this.repository = repository;
        this.proteccion = proteccion;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encolarCodigoVerificacion(String correo, String codigo) {
        if (correo == null || correo.isBlank() || codigo == null || !codigo.matches("\\d{6}")) {
            throw new DatosInvalidosException("Los datos del correo de verificación son inválidos.");
        }
        ProteccionDocumento.TextoCifrado cifrado = proteccion.cifrarTexto(codigo);
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        CorreoPendiente pendiente = new CorreoPendiente();
        pendiente.setIdCorreo(UUID.randomUUID());
        pendiente.setDestinatario(correo.trim());
        pendiente.setTipo("VERIFICACION");
        pendiente.setContenidoCifrado(cifrado.contenido());
        pendiente.setNonce(cifrado.nonce());
        pendiente.setClaveIdempotencia(UUID.randomUUID().toString());
        pendiente.setEstado("PENDIENTE");
        pendiente.setIntentos(0);
        pendiente.setProximaEjecucion(ahora);
        pendiente.setFechaCreacion(ahora);
        repository.save(pendiente);
    }

    @Transactional
    public List<CorreoParaEnviar> reclamarPendientes(int limite) {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        List<CorreoPendiente> pendientes = repository.reclamar(ahora, PageRequest.of(0, Math.max(1, Math.min(limite, 100))));
        List<CorreoParaEnviar> resultado = new ArrayList<>();
        for (CorreoPendiente p : pendientes) {
            p.setEstado("EN_PROCESO");
            p.setIntentos(p.getIntentos() + 1);
            p.setLeaseHasta(ahora.plusMinutes(2));
            resultado.add(new CorreoParaEnviar(p.getIdCorreo(), p.getDestinatario(), p.getTipo(),
                    proteccion.descifrarTexto(p.getContenidoCifrado(), p.getNonce()), p.getClaveIdempotencia()));
        }
        return resultado;
    }

    @Transactional
    public void marcarEnviado(UUID id) {
        repository.findById(id).ifPresent(p -> {
            if (!"ENVIADO".equals(p.getEstado())) {
                p.setEstado("ENVIADO");
                p.setFechaEnvio(OffsetDateTime.now(ZoneOffset.UTC));
                p.setLeaseHasta(null);
                p.setUltimoError(null);
            }
        });
    }

    @Transactional
    public void marcarError(UUID id, Exception error) {
        repository.findById(id).ifPresent(p -> {
            if ("ENVIADO".equals(p.getEstado())) return;
            OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
            int exponente = Math.min(Math.max(p.getIntentos() - 1, 0), 7);
            long demora = Math.min(3600L, 30L * (1L << exponente));
            p.setEstado("ERROR");
            p.setLeaseHasta(null);
            p.setProximaEjecucion(ahora.plusSeconds(demora));
            String mensaje = error == null ? "Error desconocido al enviar correo." : error.getClass().getSimpleName() + ": " + error.getMessage();
            p.setUltimoError(mensaje.length() > MAX_ERROR ? mensaje.substring(0, MAX_ERROR) : mensaje);
        });
    }

    public record CorreoParaEnviar(UUID id, String destinatario, String tipo, String codigo, String claveIdempotencia) { }
}
