package co.edu.unbosque.marketplace.reserva.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ReservaStripeWebhookSecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain stripeWebhookSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/reservas/webhooks/stripe")
                // Seguro sin CSRF: Stripe llama desde su servidor, sin cookies ni sesión de navegador, y
                // ReservaStripeWebhookService rechaza cualquier llamada cuya firma Stripe-Signature no sea válida.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
