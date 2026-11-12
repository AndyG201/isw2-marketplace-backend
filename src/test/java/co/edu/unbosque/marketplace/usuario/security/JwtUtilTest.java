package co.edu.unbosque.marketplace.usuario.security;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final String SECRET =
            "ClaveDePruebaJWT12345678901234567890";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Usuario usuario;

    @Mock
    private UserDetails usuarioInvalido;

    private JwtUtil jwtUtil;

    private UUID idUsuario;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(redisTemplate);

        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);

        idUsuario = UUID.randomUUID();
    }

    private void configurarUsuario(
            String autoridad,
            String versionRedis) {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(usuario.getIdUsuario()).thenReturn(idUsuario);
        when(usuario.getUsername()).thenReturn("usuario");

        doReturn(List.of(
                new SimpleGrantedAuthority(autoridad)
        )).when(usuario).getAuthorities();

        when(valueOperations.get("sesion:version:" + idUsuario))
                .thenReturn(versionRedis);
    }

    private String crearToken(
            String username,
            String rol,
            long version,
            Date expiracion) {

        return Jwts.builder()
                .setSubject(username)
                .claim("role", rol)
                .claim("sessionVersion", version)
                .setIssuedAt(new Date(System.currentTimeMillis() - 60000))
                .setExpiration(expiracion)
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        ),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    @Test
    void testGenerarToken() {
        configurarUsuario("ROLE_USER", null);

        String token = jwtUtil.generateToken(usuario);

        assertNotNull(token);
        assertEquals("usuario", jwtUtil.extractUsername(token));
        assertEquals("USER", jwtUtil.extractRole(token));
    }

    @Test
    void testGenerarTokenUsuarioInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.generateToken(usuarioInvalido)
        );
    }

    @Test
    void testGenerarTokenSinRol() {
        doReturn(List.of()).when(usuario).getAuthorities();

        assertThrows(
                IllegalStateException.class,
                () -> jwtUtil.generateToken(usuario)
        );
    }

    @Test
    void testExtractUsername() {
        configurarUsuario("ROLE_USER", null);

        String token = jwtUtil.generateToken(usuario);

        assertEquals(
                "usuario",
                jwtUtil.extractUsername(token)
        );
    }

    @Test
    void testExtractRole() {
        configurarUsuario("ROLE_USER", null);

        String token = jwtUtil.generateToken(usuario);

        assertEquals(
                "USER",
                jwtUtil.extractRole(token)
        );
    }

    @Test
    void testValidateTokenValido() {
        configurarUsuario("ROLE_USER", null);

        String token = jwtUtil.generateToken(usuario);

        assertTrue(jwtUtil.validateToken(token, usuario));
    }

    @Test
    void testValidateTokenUsuarioInvalido() {
        assertFalse(
                jwtUtil.validateToken("token", usuarioInvalido)
        );
    }

    @Test
    void testValidateTokenRolIncorrecto() {
        configurarUsuario("ROLE_ADMIN", null);

        String token = crearToken(
                "usuario",
                "USER",
                0L,
                new Date(System.currentTimeMillis() + 60000)
        );

        assertFalse(jwtUtil.validateToken(token, usuario));
    }

    @Test
    void testValidateTokenVersionIncorrecta() {
        configurarUsuario("ROLE_USER", "1");

        String token = crearToken(
                "usuario",
                "USER",
                0L,
                new Date(System.currentTimeMillis() + 60000)
        );

        assertFalse(jwtUtil.validateToken(token, usuario));
    }

    @Test
    void testValidateTokenExpirado() {

        String token = crearToken(
                "usuario",
                "USER",
                0L,
                new Date(System.currentTimeMillis() - 1000)
        );

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtUtil.validateToken(token, usuario)
        );
    }

    @Test
    void testInvalidarSesiones() {
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("sesion:version:" + idUsuario))
                .thenReturn(1L);

        assertDoesNotThrow(
                () -> jwtUtil.invalidarSesiones(idUsuario)
        );

        verify(valueOperations).increment(
                "sesion:version:" + idUsuario
        );
    }

    @Test
    void testInvalidarSesionesIdNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.invalidarSesiones(null)
        );
    }

    @Test
    void testInvalidarSesionesErrorRedis() {
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("sesion:version:" + idUsuario))
                .thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> jwtUtil.invalidarSesiones(idUsuario)
        );
    }
}