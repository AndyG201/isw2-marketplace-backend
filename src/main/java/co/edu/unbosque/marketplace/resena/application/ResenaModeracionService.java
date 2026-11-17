package co.edu.unbosque.marketplace.resena.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaResenaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoResena;
import co.edu.unbosque.marketplace.resena.api.response.OcultarResenaResponse;
import co.edu.unbosque.marketplace.resena.domain.Resena;
import co.edu.unbosque.marketplace.resena.infrastructure.persistence.ResenaModeracionRepository;

@Service
public class ResenaModeracionService {

    private static final String ACCION_OCULTAR = "OCULTAR_RESENA";
    private final ResenaModeracionRepository resenas;
    private final AuditoriaResenaCommand auditoria;
    private final ObjectMapper objectMapper;

    public ResenaModeracionService(
            ResenaModeracionRepository resenas,
            AuditoriaResenaCommand auditoria,
            ObjectMapper objectMapper) {
        this.resenas = resenas;
        this.auditoria = auditoria;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OcultarResenaResponse ocultar(UUID idAdministrador, UUID idResena, String motivo) {
        if (idAdministrador == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión como administrador.");
        }
        if (idResena == null || motivo == null || motivo.isBlank() || motivo.trim().length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes indicar un motivo de ocultamiento de máximo 2000 caracteres.");
        }

        Resena resena = resenas.bloquearPorId(idResena)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La reseña no existe o ya no está disponible."));
        if (!resena.isVisible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reseña ya está oculta y no puede modificarse.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        String motivoNormalizado = motivo.trim();
        resena.setVisible(false);
        resena.setIdAdministradorModerador(idAdministrador);
        resena.setMotivoOcultamiento(motivoNormalizado);
        resena.setFechaOcultamiento(ahora);
        Resena guardada = resenas.saveAndFlush(resena);

        auditoria.registrarEvento(new RegistrarEventoResena(
                idAdministrador,
                guardada.getIdResena(),
                ahora,
                ACCION_OCULTAR,
                "COMPLETADO",
                serializarDetalle(guardada)));

        return new OcultarResenaResponse(
                guardada.getIdResena(),
                guardada.getIdReserva(),
                guardada.isVisible(),
                guardada.getIdAdministradorModerador(),
                guardada.getMotivoOcultamiento(),
                guardada.getFechaOcultamiento(),
                "La reseña fue ocultada correctamente.");
    }

    private String serializarDetalle(Resena resena) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "idReserva", resena.getIdReserva(),
                    "motivo", resena.getMotivoOcultamiento(),
                    "visible", resena.isVisible(),
                    "fechaOcultamiento", resena.getFechaOcultamiento()));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No fue posible registrar la trazabilidad de la reseña.", error);
        }
    }
}
