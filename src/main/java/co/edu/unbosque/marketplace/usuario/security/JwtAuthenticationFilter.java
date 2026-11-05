
package co.edu.unbosque.marketplace.usuario.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            String jwt = authorizationHeader.substring(7);

            try {

                String username = jwtUtil.extractUsername(jwt);

                if (username != null
                        && SecurityContextHolder.getContext()
                                .getAuthentication() == null) {

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(username);

                    if (userDetails instanceof Usuario usuario
                            && jwtUtil.validateToken(jwt, userDetails)
                            && cuentaPuedeUsarSesion(usuario)) {

                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authenticationToken.setDetails(
                                new org.springframework.security.web.authentication
                                        .WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authenticationToken);

                    } else {
                        SecurityContextHolder.clearContext();
                    }
                }

            } catch (Exception e) {

                SecurityContextHolder.clearContext();

                logger.warn("No se pudo validar el token JWT");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean cuentaPuedeUsarSesion(Usuario usuario) {

        return usuario.isCorreoVerificado()
                && usuario.getEstado() == EstadoUsuario.ACTIVA
                && !usuario.isRestringido()
                && usuario.getIntentosFallidos() < 5;
    }
}
