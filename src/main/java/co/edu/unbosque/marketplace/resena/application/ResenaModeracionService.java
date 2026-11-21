package co.edu.unbosque.marketplace.resena.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaResenaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoResena;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.resena.application.dto.OcultarResenaResponse;
import co.edu.unbosque.marketplace.resena.application.port.ResenaModeracionRepository;
import co.edu.unbosque.marketplace.resena.domain.Resena;

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
            throw new SesionRequeridaException("Debes iniciar sesión como administrador.");
        }
        if (idResena == null || motivo == null || motivo.isBlank() || motivo.trim().length() > 2000) {
            throw new DatosInvalidosException(
                    "Debes indicar un motivo de ocultamiento de máximo 2000 caracteres.");
        }

        Resena resena = resenas.bloquearPorId(idResena)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La reseña no existe o ya no está disponible."));
        if (!resena.isVisible()) {
            throw new ConflictoNegocioException("La reseña ya está oculta y no puede modificarse.");
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
