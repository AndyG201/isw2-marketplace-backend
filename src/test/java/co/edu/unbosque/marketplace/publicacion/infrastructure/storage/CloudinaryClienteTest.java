package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CloudinaryClienteTest {

    private static final String URL = "cloudinary://clave:secreto@demo";
    private static final Clock RELOJ =
            Clock.fixed(Instant.ofEpochSecond(1_700_000_000L), ZoneOffset.UTC);

    private RestClient.Builder constructor;
    private MockRestServiceServer servidor;

    @BeforeEach
    void preparar() {
        constructor = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(constructor).build();
    }

    private CloudinaryCliente cliente(String url) {
        return new CloudinaryCliente(url, "sav/publicaciones", constructor, RELOJ);
    }

    private static String firma(String texto) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(texto.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void sinUrlValidaNoQuedaConfigurado() {
        assertFalse(cliente("").configurado());
        assertFalse(cliente(null).configurado());
        assertFalse(cliente("https://demo").configurado());
        assertFalse(cliente("").esUrlPropia("https://res.cloudinary.com/demo/image/upload/a.jpg"));
    }

    @Test
    void subeLaImagenFirmadaYDevuelveSuDireccion() throws Exception {
        String esperada = firma("public_id=sav/publicaciones/foto&timestamp=1700000000secreto");

        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/upload"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().multipartDataContains(Map.of(
                        "public_id", "sav/publicaciones/foto",
                        "timestamp", "1700000000",
                        "api_key", "clave",
                        "signature", esperada)))
                .andRespond(withSuccess(
                        "{\"secure_url\":\"https://res.cloudinary.com/demo/image/upload/v1/sav/publicaciones/foto.jpg\"}",
                        MediaType.APPLICATION_JSON));

        String url = cliente(URL).subir(new byte[] {1, 2, 3}, "foto", ".jpg");

        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/sav/publicaciones/foto.jpg", url);
        servidor.verify();
    }

    @Test
    void limpiaLasBarrasDeLaCarpetaYPermiteNoUsarCarpeta() {
        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/upload"))
                .andExpect(content().multipartDataContains(Map.of("public_id", "sav/fotos/foto")))
                .andRespond(withSuccess("{\"secure_url\":\"https://x\"}", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/upload"))
                .andExpect(content().multipartDataContains(Map.of("public_id", "foto")))
                .andRespond(withSuccess("{\"secure_url\":\"https://y\"}", MediaType.APPLICATION_JSON));

        new CloudinaryCliente(URL, " //sav/fotos/ ", constructor, RELOJ).subir(new byte[] {1}, "foto", ".jpg");
        new CloudinaryCliente(URL, null, constructor, RELOJ).subir(new byte[] {1}, "foto", ".jpg");

        servidor.verify();
    }

    @Test
    void fallaSiCloudinaryNoDevuelveLaDireccion() {
        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/upload"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        CloudinaryCliente cliente = cliente(URL);
        byte[] contenido = {1};

        assertThrows(IllegalStateException.class,
                () -> cliente.subir(contenido, "foto", ".jpg"));
    }

    @Test
    void informaCuandoCloudinaryFalla() {
        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/upload"))
                .andRespond(withServerError());

        CloudinaryCliente cliente = cliente(URL);
        byte[] contenido = {1};

        assertThrows(IllegalStateException.class,
                () -> cliente.subir(contenido, "foto", ".jpg"));
    }

    @Test
    void eliminaUnaImagenPropiaPorSuIdentificador() throws Exception {
        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/destroy"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(Map.of(
                        "public_id", "sav/publicaciones/foto",
                        "api_key", "clave",
                        "signature", firma("public_id=sav/publicaciones/foto&timestamp=1700000000secreto"))))
                .andRespond(withSuccess("{\"result\":\"ok\"}", MediaType.APPLICATION_JSON));

        cliente(URL).eliminar("https://res.cloudinary.com/demo/image/upload/v1712345678/sav/publicaciones/foto.jpg");

        servidor.verify();
    }

    @Test
    void reconoceSoloLasDireccionesDeSuCuenta() {
        CloudinaryCliente cliente = cliente(URL);

        assertTrue(cliente.esUrlPropia("https://res.cloudinary.com/demo/image/upload/v1/a.jpg"));
        assertFalse(cliente.esUrlPropia("https://res.cloudinary.com/otra/image/upload/v1/a.jpg"));
        assertFalse(cliente.esUrlPropia("/imagenes/publicaciones/a.jpg"));
        assertEquals("sav/a", cliente.idPublico("https://res.cloudinary.com/demo/image/upload/sav/a.png"));
        assertNull(cliente.idPublico("https://otro.com/a.jpg"));
        assertEquals("sav/sin-extension", cliente.idPublico("https://res.cloudinary.com/demo/image/upload/v9/sav/sin-extension"));
        assertEquals("sav.v2/foto", cliente.idPublico("https://res.cloudinary.com/demo/image/upload/sav.v2/foto"));
        assertNull(cliente.idPublico("https://res.cloudinary.com/demo/video/upload/a.mp4"));
        assertNull(cliente.idPublico("https://res.cloudinary.com/demo/image/upload/v3/"));

        cliente.eliminar("https://otro.com/a.jpg");
        servidor.verify();
    }

    @Test
    void informaCuandoNoPuedeEliminar() {
        servidor.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/destroy"))
                .andRespond(withServerError());

        CloudinaryCliente cliente = cliente(URL);

        assertThrows(IllegalStateException.class,
                () -> cliente.eliminar("https://res.cloudinary.com/demo/image/upload/v1/sav/a.jpg"));
    }
}
