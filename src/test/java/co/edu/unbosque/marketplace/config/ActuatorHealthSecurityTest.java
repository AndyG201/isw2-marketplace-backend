package co.edu.unbosque.marketplace.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.usuario.security.JwtAuthenticationFilter;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.SecurityConfig;

/**
 * HTTP y seguridad reales de Actuator, con indicadores de prueba para no depender
 * de PostgreSQL/Redis externos. No carga MarketplaceApplication ni sus servicios.
 */
@SpringJUnitWebConfig(
        classes = ActuatorHealthSecurityTest.ProbeTestConfiguration.class,
        initializers = ActuatorHealthSecurityTest.BootConversionInitializer.class)
@TestPropertySource(locations = "classpath:application-observability.properties")
class ActuatorHealthSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DependencyState dependencies;

    @Autowired
    private WebEndpointsSupplier webEndpoints;

    private MockMvc mvc;

    @BeforeEach
    void prepararSondas() {
        dependencies.databaseUp = true;
        dependencies.redisUp = true;
        AvailabilityChangeEvent.publish(context, LivenessState.CORRECT);
        AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", FilterChainProxy.class))
                .build();
    }

    @Test
    void healthPermiteConsultaAnonimaSinDatosInternos() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void readinessPermiteConsultaAnonimaCuandoLasDependenciasEstanDisponibles() throws Exception {
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void caidaDeBaseDeDatosAfectaReadinessPeroNoLiveness() throws Exception {
        dependencies.databaseUp = false;

        comprobarNoListoPeroVivo();
    }

    @Test
    void caidaDeRedisAfectaReadinessPeroNoLiveness() throws Exception {
        dependencies.redisUp = false;

        comprobarNoListoPeroVivo();
    }

    @Test
    void soloHealthSeExponeComoEndpointHttp() {
        assertThat(webEndpoints.getEndpoints())
                .extracting(endpoint -> endpoint.getEndpointId().toString())
                .containsExactly("health");
    }

    @Test
    void abrirHealthNoPermiteOtrosActuatorNiOperacionesDeEscritura() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/administracion/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(post("/actuator/health")).andExpect(status().isUnauthorized());
    }

    private void comprobarNoListoPeroVivo() throws Exception {
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components").doesNotExist());
        mvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    // El contexto de prueba no arranca SpringApplication: incorpora su conversion
    // de propiedades (por ejemplo, "read-only" -> Access.READ_ONLY).
    static class BootConversionInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.getEnvironment().setConversionService(new ApplicationConversionService());
        }
    }

    static class DependencyState {
        volatile boolean databaseUp = true;
        volatile boolean redisUp = true;
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class,
            RedisAutoConfiguration.class,
            RedisReactiveAutoConfiguration.class,
            RedisRepositoriesAutoConfiguration.class
    })
    @Import(SecurityConfig.class)
    static class ProbeTestConfiguration {

        @Bean
        DependencyState dependencyState() {
            return new DependencyState();
        }

        @Bean
        HealthIndicator dbHealthIndicator(DependencyState state) {
            return () -> (state.databaseUp ? Health.up() : Health.down())
                    .withDetail("database", "detalle que no debe publicarse")
                    .build();
        }

        @Bean
        HealthIndicator redisHealthIndicator(DependencyState state) {
            return () -> (state.redisUp ? Health.up() : Health.down())
                    .withDetail("redis", "detalle que no debe publicarse")
                    .build();
        }

        @Bean
        UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService users) {
            return new JwtAuthenticationFilter(mock(JwtUtil.class), users);
        }

        @Bean
        AuditoriaEventoCommand auditoriaEventoCommand() {
            return mock(AuditoriaEventoCommand.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
