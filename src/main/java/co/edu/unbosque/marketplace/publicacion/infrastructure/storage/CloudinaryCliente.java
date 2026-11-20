package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class CloudinaryCliente {

    private static final Pattern URL_CONFIGURACION =
            Pattern.compile("^cloudinary://([^:]+):([^@]+)@([^/?#]+)$");

    private static final Pattern RUTA_PUBLICA =
            Pattern.compile("/image/upload/(?:v\\d+/)?(.+?)(?:\\.[a-zA-Z0-9]+)?$");

    private final RestClient cliente;
    private final Clock reloj;
    private final String apiKey;
    private final String apiSecret;
    private final String nombreNube;
    private final String carpeta;

    @Autowired
    public CloudinaryCliente(
            @Value("${app.storage.cloudinary-url:}") String urlConfiguracion,
            @Value("${app.storage.cloudinary-carpeta:sav/publicaciones}") String carpeta,
            RestClient.Builder constructor) {
        this(urlConfiguracion, carpeta, constructor, Clock.systemUTC());
    }

    CloudinaryCliente(
            String urlConfiguracion,
            String carpeta,
            RestClient.Builder constructor,
            Clock reloj) {

        Matcher datos = URL_CONFIGURACION.matcher(
                urlConfiguracion == null ? "" : urlConfiguracion.trim());

        boolean valida = datos.matches();

        this.apiKey = valida ? datos.group(1) : null;
        this.apiSecret = valida ? datos.group(2) : null;
        this.nombreNube = valida ? datos.group(3) : null;
        this.carpeta = carpeta == null ? "" : carpeta.trim().replaceAll("^/+|/+$", "");
        this.reloj = reloj;
        this.cliente = constructor
                .baseUrl("https://api.cloudinary.com/v1_1/" + (valida ? nombreNube : "sin-configurar"))
                .build();
    }

    public boolean configurado() {
        return nombreNube != null;
    }

    public boolean esUrlPropia(String ruta) {
        return configurado()
                && ruta != null
                && ruta.startsWith("https://res.cloudinary.com/" + nombreNube + "/");
    }

    public String subir(byte[] contenido, String nombre, String extension) {

        String publicId = carpeta.isEmpty() ? nombre : carpeta + "/" + nombre;

        Map<String, String> parametros = firmar(Map.of("public_id", publicId));

        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        parametros.forEach(cuerpo::add);
        cuerpo.add("file", new ByteArrayResource(contenido) {
            @Override
            public String getFilename() {
                return nombre + extension;
            }
        });

        try {

            JsonNode respuesta = cliente.post()
                    .uri("/image/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);

            String url = respuesta == null ? "" : respuesta.path("secure_url").asText("");

            if (url.isBlank()) {
                throw new IllegalStateException(
                        "Cloudinary no devolvió la dirección de la imagen.");
            }

            return url;

        } catch (RestClientException e) {

            throw new IllegalStateException(
                    "No fue posible almacenar la imagen.", e);
        }
    }

    public void eliminar(String url) {

        String publicId = idPublico(url);

        if (publicId == null) {
            return;
        }

        MultiValueMap<String, String> cuerpo = new LinkedMultiValueMap<>();
        firmar(Map.of("public_id", publicId)).forEach(cuerpo::add);

        try {

            cliente.post()
                    .uri("/image/destroy")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {

            throw new IllegalStateException(
                    "No fue posible eliminar la imagen.", e);
        }
    }

    String idPublico(String url) {

        if (!esUrlPropia(url)) {
            return null;
        }

        Matcher datos = RUTA_PUBLICA.matcher(URI.create(url).getPath());

        return datos.find() ? datos.group(1) : null;
    }

    private Map<String, String> firmar(Map<String, String> parametros) {

        TreeMap<String, String> firmados = new TreeMap<>(parametros);
        firmados.put("timestamp", String.valueOf(reloj.instant().getEpochSecond()));

        String texto = firmados.entrySet().stream()
                .map(entrada -> entrada.getKey() + "=" + entrada.getValue())
                .collect(Collectors.joining("&"));

        firmados.put("signature", sha256(texto + apiSecret));
        firmados.put("api_key", apiKey);

        return firmados;
    }

    private static String sha256(String texto) {

        try {

            byte[] resumen = MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(resumen);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException("SHA-256 no está disponible.", e);
        }
    }
}
