package co.edu.unbosque.marketplace.auditoria.api;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.auditoria.api.response.EventoAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.api.response.PaginaAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.application.AuditoriaConsultaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/auditoria/eventos")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaConsultaController {

    private final AuditoriaConsultaService auditoria;

    public AuditoriaConsultaController(AuditoriaConsultaService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    public PaginaAuditoriaResponse consultar(
            @RequestParam(required = false) UUID idUsuario,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) String entidadAfectada,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return auditoria.consultar(
                idUsuario, desde, hasta, accion, resultado,
                entidadAfectada, pagina, tamano);
    }

    @GetMapping("/{idEvento}")
    public EventoAuditoriaResponse consultarPorId(@PathVariable UUID idEvento) {
        return auditoria.consultar(idEvento);
    }
}