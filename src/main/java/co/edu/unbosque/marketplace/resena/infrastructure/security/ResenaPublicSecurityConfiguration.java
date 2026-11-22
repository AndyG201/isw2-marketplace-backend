package co.edu.unbosque.marketplace.resena.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

@Configuration
public class ResenaPublicSecurityConfiguration {

	private static final String RUTA_RESEÑAS = "/publicaciones/*/resenas";
	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
	private static final RequestMatcher CONSULTA_PUBLICA = request -> HttpMethod.GET.matches(request.getMethod())
			&& PATH_MATCHER.match(RUTA_RESEÑAS, request.getServletPath());

	@Bean
	@Order(2)
	SecurityFilterChain resenasPublicasSecurityFilterChain(HttpSecurity http) throws Exception {
		return http.securityMatcher(CONSULTA_PUBLICA).cors(cors -> {
		}).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
	}
}
