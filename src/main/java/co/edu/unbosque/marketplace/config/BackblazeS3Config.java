package co.edu.unbosque.marketplace.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Configura el cliente S3 para establecer la conexión
 * entre Marketplace y Backblaze B2.
 *
 * Utiliza AWS SDK for Java 2.x para acceder al almacenamiento
 * de objetos mediante la API compatible con Amazon S3.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "app.auditoria.b2",
    name = "enabled",
    havingValue = "true"
)
public class BackblazeS3Config {

    /**
     * Construye el cliente S3 con las credenciales y parámetros
     * de conexión definidos en application-local.properties.
     *
     * @param endpoint dirección del servicio Backblaze B2.
     * @param region región del bucket.
     * @param accessKey identificador de la aplicación.
     * @param secretKey clave secreta de la aplicación.
     * @param pathStyleAccess indica si se utiliza acceso por ruta.
     * @return cliente S3 configurado para Backblaze B2.
     */
    @Bean
    public S3Client backblazeS3Client(
            @Value("${app.auditoria.b2.endpoint}") String endpoint,
            @Value("${app.auditoria.b2.region}") String region,
            @Value("${app.auditoria.b2.access-key}") String accessKey,
            @Value("${app.auditoria.b2.secret-key}") String secretKey,
            @Value("${app.auditoria.b2.path-style-access:true}") boolean pathStyleAccess) {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(credentials)
                )
                .serviceConfiguration(
                    S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleAccess)
                        .build()
                )
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }
}
