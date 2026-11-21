
package co.edu.unbosque.marketplace.auditoria.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.ResultadoAlmacenamientoAuditoria;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@ExtendWith(MockitoExtension.class)
class BackblazeAuditoriaAdapterTest {

    @Mock
    private S3Client s3Client;

    private BackblazeAuditoriaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BackblazeAuditoriaAdapter(
                s3Client,
                new ObjectMapper(),
                "marketplace-auditoria",
                365
        );
    }

    @Test
    void almacenaEventoConRetencionCompliance() {

        BandejaSalidaAuditoria evento = evento();

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(
                        S3Exception.builder()
                                .statusCode(404)
                                .message("Not found")
                                .build()
                );

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(
                PutObjectResponse.builder()
                        .versionId("version-test-123")
                        .build()
        );

        Instant retencionHasta = Instant.now()
                .plus(366, ChronoUnit.DAYS);

        when(s3Client.getObjectRetention(
                any(GetObjectRetentionRequest.class)
        )).thenReturn(
                GetObjectRetentionResponse.builder()
                        .retention(
                                ObjectLockRetention.builder()
                                        .mode(ObjectLockRetentionMode.COMPLIANCE)
                                        .retainUntilDate(retencionHasta)
                                        .build()
                        )
                        .build()
        );

        ResultadoAlmacenamientoAuditoria resultado =
                adapter.almacenar(evento);

        assertNotNull(resultado);
        assertEquals("COMPLIANCE", resultado.modoRetencion());
        assertEquals("version-test-123", resultado.versionId());
        assertEquals(64, resultado.sha256().length());

        ArgumentCaptor<PutObjectRequest> captor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(
                captor.capture(),
                any(RequestBody.class)
        );

        PutObjectRequest solicitud = captor.getValue();

        assertEquals("marketplace-auditoria", solicitud.bucket());
        assertEquals(ObjectLockMode.COMPLIANCE, solicitud.objectLockMode());
        assertEquals("application/json", solicitud.contentType());

        assertEquals(
                evento.getIdEvento().toString(),
                solicitud.metadata().get("id-evento")
        );

        assertNotNull(solicitud.objectLockRetainUntilDate());
    }

    @Test
    void rechazaEventoNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.almacenar(null)
        );

        verifyNoInteractions(s3Client);
    }

    private BandejaSalidaAuditoria evento() {

        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();

        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(UUID.randomUUID());
        evento.setFechaEvento(OffsetDateTime.now());
        evento.setAccion("PUBLICACION_APROBADA");
        evento.setResultado("EXITOSO");
        evento.setEntidadAfectada("PUBLICACION");
        evento.setIdEntidadAfectada(UUID.randomUUID());
        evento.setDetalle("{\"estado\":\"ACTIVA\"}");
        evento.setIdCorrelacion("corr-test");

        return evento;
    }
}
