package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.config.PasswordConfig;
import co.edu.unbosque.marketplace.usuario.security.JwtAuthenticationFilter;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.SecurityConfig;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        PasswordConfig.class,
        JwtAuthenticationFilter.class,
        ImagenPublicacionWebConfig.class
})
class ImagenPublicacionAccesoPublicoTest {

    @TempDir
    static Path directorio;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuditoriaEventoCommand auditoriaEventoCommand;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry registro) {
        registro.add("app.storage.upload-dir", () -> directorio.toString());
    }

    @BeforeAll
    static void crearImagen() throws IOException {
        Path publicaciones = Files.createDirectories(directorio.resolve("publicaciones"));
        Files.write(publicaciones.resolve("portada.jpg"), new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    }

    @Test
    void permiteVerLasImagenesSinIniciarSesion() throws Exception {
        mockMvc.perform(get("/imagenes/publicaciones/portada.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}));
    }

    @Test
    void unaImagenInexistenteNoPideSesion() throws Exception {
        mockMvc.perform(get("/imagenes/publicaciones/no-existe.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void soloLaConsultaEsPublica() throws Exception {
        mockMvc.perform(delete("/imagenes/publicaciones/portada.jpg"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lasDemasRutasSiguenPidiendoSesion() throws Exception {
        mockMvc.perform(get("/usuarios/mi_cuenta"))
                .andExpect(status().isUnauthorized());
    }
}
