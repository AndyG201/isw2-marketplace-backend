
package co.edu.unbosque.marketplace.usuario.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final long JWT_TOKEN_VALIDITY = 30 * 60 * 1000L;

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String secret;

    public JwtUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private Key getSigningKey() {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    public String extractRole(String token) {

        return extractClaim(
                token,
                claims -> claims.get("role", String.class)
        );
    }

    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {

        if (!(userDetails instanceof Usuario usuario)) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al usuario."
            );
        }

        List<String> authorities =
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

        if (authorities.isEmpty()) {
            throw new IllegalStateException(
                    "El usuario no tiene un rol asignado."
            );
        }

        String autoridadPrincipal = authorities.get(0);

        String rol = autoridadPrincipal.startsWith("ROLE_")
                ? autoridadPrincipal.substring(5)
                : autoridadPrincipal;

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", rol);
        claims.put("authorities", authorities);
        claims.put(
                "sessionVersion",
                obtenerVersionSesion(usuario.getIdUsuario())
        );

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(
            Map<String, Object> claims,
            String subject) {

        long ahora = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(
            String token,
            UserDetails userDetails) {

        if (!(userDetails instanceof Usuario usuario)) {
            return false;
        }

        String username = extractUsername(token);
        String rolToken = extractRole(token);

        Object versionToken = extractClaim(
                token,
                claims -> claims.get("sessionVersion")
        );

        if (!(versionToken instanceof Number)) {
            return false;
        }

        long versionActual =
                obtenerVersionSesion(usuario.getIdUsuario());

        boolean rolValido =
                rolToken != null
                && userDetails.getAuthorities()
                        .stream()
                        .anyMatch(authority -> {

                            String nombreAutoridad =
                                    authority.getAuthority();

                            String rolActual =
                                    nombreAutoridad.startsWith("ROLE_")
                                            ? nombreAutoridad.substring(5)
                                            : nombreAutoridad;

                            return rolActual.equals(rolToken);
                        });

        return username.equals(userDetails.getUsername())
                && rolValido
                && ((Number) versionToken).longValue() == versionActual
                && !isTokenExpired(token);
    }

    private long obtenerVersionSesion(UUID idUsuario) {

        String clave = "sesion:version:" + idUsuario;

        String version = redisTemplate.opsForValue().get(clave);

        return version == null ? 0L : Long.parseLong(version);
    }

    public void invalidarSesiones(UUID idUsuario) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al usuario."
            );
        }

        Long nuevaVersion = redisTemplate.opsForValue()
                .increment("sesion:version:" + idUsuario);

        if (nuevaVersion == null) {
            throw new IllegalStateException(
                    "No fue posible invalidar las sesiones."
            );
        }
    }
}
