package co.edu.unbosque.marketplace.auditoria.infrastructure.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@ConditionalOnProperty(
    prefix = "app.auditoria.b2",
    name = "enabled",
    havingValue = "true"
)
public class BackblazeAuditoriaService {

    private static final DateTimeFormatter FORMATO_RUTA =
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
                    .withZone(ZoneOffset.UTC);

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucket;
    private final long retentionDays;

    public BackblazeAuditoriaService(
            S3Client s3Client,
            ObjectMapper objectMapper,
            @Value("${app.auditoria.b2.bucket}") String bucket,
            @Value("${app.auditoria.b2.retention-days}") long retentionDays) {

        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucket = bucket;
        this.retentionDays = retentionDays;
    }

    /**
     * Almacena un evento como objeto JSON inmutable.
     */
    public ResultadoAlmacenamientoAuditoria almacenar(
            BandejaSalidaAuditoria evento) {

        validarEvento(evento);
        validarConfiguracion();

        try {
            byte[] contenido = serializarEvento(evento);
            String hash = calcularSha256(contenido);
            String clave = construirClave(evento);

            HeadObjectResponse existente = buscarObjeto(clave);

            // Idempotencia: si ya existe, se verifica y reutiliza.
            if (existente != null) {

                String hashExistente = existente.metadata().get("sha256");

                if (!hash.equals(hashExistente)) {
                    throw new IllegalStateException(
                            "El evento ya existe en Backblaze con un contenido diferente.");
                }

                ObjectLockRetention retencion =
                        asegurarRetencion(clave, existente.versionId());

                return crearResultado(
                        clave, hash, retencion, existente.versionId());
            }

            Instant fechaRetencion = Instant.now()
                    .plus(retentionDays, ChronoUnit.DAYS);

            PutObjectRequest solicitud = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(clave)
                    .contentType("application/json")
                    .metadata(Map.of(
                            "id-evento", evento.getIdEvento().toString(),
                            "sha256", hash
                    ))
                    .objectLockMode(ObjectLockMode.COMPLIANCE)
                    .objectLockRetainUntilDate(fechaRetencion)
                    .build();

            PutObjectResponse respuesta = s3Client.putObject(
                    solicitud,
                    RequestBody.fromBytes(contenido)
            );

            String versionId = respuesta.versionId();

            if (versionId == null || versionId.isBlank()) {
                HeadObjectResponse head = buscarObjeto(clave);
                versionId = head == null ? null : head.versionId();
            }

            ObjectLockRetention retencion =
                    asegurarRetencion(clave, versionId);

            if (retencion.retainUntilDate().isBefore(
                    fechaRetencion.minusSeconds(2))) {

                throw new IllegalStateException(
                        "Backblaze no aplicó el período de retención solicitado.");
            }

            return crearResultado(
                    clave, hash, retencion, versionId);

        } catch (SdkException error) {
            throw new IllegalStateException(
                    "No fue posible almacenar el evento en Backblaze B2.",
                    error
            );
        }
    }

    /**
     * Recupera el evento original y verifica su SHA-256.
     */
    public EventoAuditoria obtenerEvento(
            String claveObjeto,
            String versionId,
            String hashEsperado,
            UUID idEventoEsperado) {

        if (claveObjeto == null || hashEsperado == null
                || idEventoEsperado == null) {
            throw new IllegalArgumentException(
                    "Los datos de consulta del evento son obligatorios.");
        }

        GetObjectRequest.Builder solicitud = GetObjectRequest.builder()
                .bucket(bucket)
                .key(claveObjeto);

        if (versionId != null && !versionId.isBlank()) {
            solicitud.versionId(versionId);
        }

        try (ResponseInputStream<GetObjectResponse> respuesta =
                s3Client.getObject(solicitud.build())) {

            byte[] contenido = respuesta.readAllBytes();
            String hashActual = calcularSha256(contenido);

            if (!MessageDigest.isEqual(
                    hashActual.getBytes(StandardCharsets.US_ASCII),
                    hashEsperado.getBytes(StandardCharsets.US_ASCII))) {

                throw new IllegalStateException(
                        "La integridad del evento de auditoría no pudo verificarse.");
            }

            JsonNode documento = objectMapper.readTree(contenido);

            UUID idEvento = UUID.fromString(
                    textoObligatorio(documento, "idEvento"));

            if (!idEventoEsperado.equals(idEvento)) {
                throw new IllegalStateException(
                        "El identificador del evento almacenado no coincide.");
            }

            UUID idUsuario = uuidOpcional(documento, "idUsuario");
            UUID idEntidad = uuidOpcional(documento, "idEntidadAfectada");

            OffsetDateTime fechaEvento = OffsetDateTime.parse(
                    textoObligatorio(documento, "fechaEvento"));

            JsonNode detalle = documento.get("detalle");

            String detalleJson = detalle == null || detalle.isNull()
                    ? null
                    : detalle.toString();

            return new EventoAuditoria(
                    idEvento,
                    idUsuario,
                    fechaEvento,
                    textoObligatorio(documento, "accion"),
                    textoObligatorio(documento, "resultado"),
                    textoObligatorio(documento, "entidadAfectada"),
                    idEntidad,
                    detalleJson,
                    textoOpcional(documento, "idCorrelacion")
            );

        } catch (IOException | SdkException error) {
            throw new IllegalStateException(
                    "No fue posible recuperar el evento de auditoría desde Backblaze.",
                    error
            );
        }
    }

    private HeadObjectResponse buscarObjeto(String clave) {

        try {
            return s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(clave)
                            .build()
            );

        } catch (S3Exception error) {
            if (error.statusCode() == 404) {
                return null;
            }
            throw error;
        }
    }

    /**
     * Comprueba que el objeto tenga retención COMPLIANCE.
     * Si el objeto existente no tiene retención o esta venció,
     * establece una nueva protección.
     */
    private ObjectLockRetention asegurarRetencion(
            String clave,
            String versionId) {

        GetObjectRetentionRequest.Builder consulta =
                GetObjectRetentionRequest.builder()
                        .bucket(bucket)
                        .key(clave);

        if (versionId != null && !versionId.isBlank()) {
            consulta.versionId(versionId);
        }

        ObjectLockRetention retencion =
                s3Client.getObjectRetention(consulta.build()).retention();

        if (retencion != null
                && retencion.mode() != null
                && retencion.mode() != ObjectLockRetentionMode.COMPLIANCE) {

            throw new IllegalStateException(
                    "El objeto existente no tiene retención COMPLIANCE.");
        }

        if (retencion == null
                || retencion.retainUntilDate() == null
                || !retencion.retainUntilDate().isAfter(Instant.now())) {

            Instant nuevaFecha = Instant.now()
                    .plus(retentionDays, ChronoUnit.DAYS);

            PutObjectRetentionRequest.Builder actualizacion =
                    PutObjectRetentionRequest.builder()
                            .bucket(bucket)
                            .key(clave)
                            .retention(
                                ObjectLockRetention.builder()
                                    .mode(ObjectLockRetentionMode.COMPLIANCE)
                                    .retainUntilDate(nuevaFecha)
                                    .build()
                            );

            if (versionId != null && !versionId.isBlank()) {
                actualizacion.versionId(versionId);
            }

            s3Client.putObjectRetention(actualizacion.build());

            retencion = s3Client.getObjectRetention(consulta.build())
                    .retention();
        }

        if (retencion == null
                || retencion.mode() != ObjectLockRetentionMode.COMPLIANCE
                || retencion.retainUntilDate() == null
                || !retencion.retainUntilDate().isAfter(Instant.now())) {

            throw new IllegalStateException(
                    "No se pudo comprobar la retención inmutable del evento.");
        }

        return retencion;
    }

    private ResultadoAlmacenamientoAuditoria crearResultado(
            String clave,
            String hash,
            ObjectLockRetention retencion,
            String versionId) {

        return new ResultadoAlmacenamientoAuditoria(
                clave,
                hash,
                retencion.mode().toString(),
                OffsetDateTime.ofInstant(
                        retencion.retainUntilDate(), ZoneOffset.UTC),
                versionId
        );
    }

    private String construirClave(BandejaSalidaAuditoria evento) {

        String fecha = FORMATO_RUTA.format(
                evento.getFechaEvento().toInstant());

        return "eventos/" + fecha + "/"
                + evento.getIdEvento() + ".json";
    }

    private byte[] serializarEvento(BandejaSalidaAuditoria evento) {

        try {
            ObjectNode documento = objectMapper.createObjectNode();

            documento.put("versionEsquema", 1);
            documento.put("idEvento", evento.getIdEvento().toString());

            if (evento.getIdUsuario() != null) {
                documento.put("idUsuario", evento.getIdUsuario().toString());
            } else {
                documento.putNull("idUsuario");
            }

            documento.put(
                    "fechaEvento",
                    evento.getFechaEvento().toInstant().toString());

            documento.put("accion", evento.getAccion());
            documento.put("resultado", evento.getResultado());
            documento.put("entidadAfectada", evento.getEntidadAfectada());

            if (evento.getIdEntidadAfectada() != null) {
                documento.put(
                        "idEntidadAfectada",
                        evento.getIdEntidadAfectada().toString());
            } else {
                documento.putNull("idEntidadAfectada");
            }

            if (evento.getDetalle() != null
                    && !evento.getDetalle().isBlank()) {

                documento.set(
                        "detalle",
                        objectMapper.readTree(evento.getDetalle()));

            } else {
                documento.putNull("detalle");
            }

            documento.put("idCorrelacion", evento.getIdCorrelacion());

            return objectMapper.writeValueAsBytes(documento);

        } catch (JsonProcessingException error) {
            throw new IllegalStateException(
                    "No fue posible serializar el evento de auditoría.",
                    error);
        }
    }

    private String calcularSha256(byte[] contenido) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(contenido));

        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(
                    "No está disponible el algoritmo SHA-256.", error);
        }
    }

    private UUID uuidOpcional(JsonNode documento, String campo) {
        String valor = textoOpcional(documento, campo);
        return valor == null ? null : UUID.fromString(valor);
    }

    private String textoObligatorio(JsonNode documento, String campo) {

        String valor = textoOpcional(documento, campo);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "El documento de auditoría no contiene el campo " + campo);
        }

        return valor;
    }

    private String textoOpcional(JsonNode documento, String campo) {

        JsonNode valor = documento.get(campo);

        return valor == null || valor.isNull()
                ? null
                : valor.asText();
    }

    private void validarEvento(BandejaSalidaAuditoria evento) {

        if (evento == null
                || evento.getIdEvento() == null
                || evento.getFechaEvento() == null
                || evento.getAccion() == null
                || evento.getResultado() == null
                || evento.getEntidadAfectada() == null) {

            throw new IllegalArgumentException(
                    "El evento de auditoría contiene campos obligatorios nulos.");
        }
    }

    private void validarConfiguracion() {

        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException(
                    "No se configuró el bucket de auditoría.");
        }

        if (retentionDays < 1 || retentionDays > 3000) {
            throw new IllegalStateException(
                    "La retención debe estar entre 1 y 3000 días.");
        }
    }
}
