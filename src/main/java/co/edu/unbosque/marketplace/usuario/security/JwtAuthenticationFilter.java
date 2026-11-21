
package co.edu.unbosque.marketplace.usuario.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private AuditoriaEventoCommand auditoria;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader =
                request.getHeader("Authorization");

        logger.info("[JWT] Peticion: "
                + request.getMethod()
                + " "
                + request.getRequestURI());

        logger.info("[JWT] Authorization presente: "
                + (authorizationHeader != null));

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            String jwt = authorizationHeader.substring(7);

            try {

                String username = jwtUtil.extractUsername(jwt);

                logger.info("[JWT] Usuario extraido correctamente.");

                if (username != null
                        && SecurityContextHolder.getContext()
                                .getAuthentication() == null) {

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(username);

                    boolean tokenValido =
                            jwtUtil.validateToken(jwt, userDetails);

                    boolean cuentaHabilitada = false;

                    if (userDetails instanceof Usuario usuario) {
                        cuentaHabilitada =
                                cuentaPuedeUsarSesion(usuario);
                    }

                    logger.info("[JWT] Token valido: " + tokenValido);

                    logger.info("[JWT] Cuenta habilitada: "
                            + cuentaHabilitada);

                    logger.info("[JWT] Autoridades: "
                            + userDetails.getAuthorities());

                    if (userDetails instanceof Usuario
                            && tokenValido
                            && cuentaHabilitada) {

                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authenticationToken);

                        logger.info("[JWT] Autenticacion establecida correctamente.");

                    } else {

                        UUID idUsuario = userDetails instanceof Usuario usuario
                                ? usuario.getIdUsuario() : null;
                        registrarFalloToken(request.getRequestURI(), "TOKEN_RECHAZADO", idUsuario);
                        SecurityContextHolder.clearContext();

                        logger.warn("[JWT] Autenticacion rechazada.");
                    }
                }

            } catch (Exception e) {

                registrarFalloToken(request.getRequestURI(), "TOKEN_INVALIDO", null);
                SecurityContextHolder.clearContext();

                logger.error("[JWT] Error validando autenticacion: "
                        + e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void registrarFalloToken(String ruta, String motivo, UUID idUsuario) {
        if (auditoria == null) {
            return;
        }
        try {
            auditoria.registrarEventoIndependiente(new RegistrarEventoAuditoria(
                    idUsuario, OffsetDateTime.now(ZoneOffset.UTC),
                    "AUTENTICACION_TOKEN", "FALLIDO", "ENDPOINT", null,
                    "{\"motivo\":\"" + motivo + "\"}", null));
        } catch (RuntimeException ignored) {
            // El rechazo del token debe continuar aunque falle el registro de auditoría.
        }
    }

    private boolean cuentaPuedeUsarSesion(Usuario usuario) {

        return usuario.isCorreoVerificado()
                && usuario.getEstado() == EstadoUsuario.ACTIVA
                && !usuario.isRestringido()
                && usuario.getIntentosFallidos() < 5;
    }
}
