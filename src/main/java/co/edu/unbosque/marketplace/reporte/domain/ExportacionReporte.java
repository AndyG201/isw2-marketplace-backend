package co.edu.unbosque.marketplace.reporte.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exportacion_reporte")
public class ExportacionReporte {

    @Id
    @Column(name = "id_exportacion", nullable = false)
    private UUID idExportacion;

    @Column(name = "id_administrador", nullable = false)
    private UUID idAdministrador;

    @Column(name = "tipo_reporte", length = 50, nullable = false)
    private String tipoReporte;

    @Column(name = "formato", length = 10, nullable = false)
    private String formato;

    @Column(name = "filtros", columnDefinition = "jsonb")
    private String filtros;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private OffsetDateTime fechaSolicitud;

    @Column(name = "fecha_corte_datos")
    private OffsetDateTime fechaCorteDatos;

    @Column(name = "fecha_generacion")
    private OffsetDateTime fechaGeneracion;

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo;

    public ExportacionReporte() {
    }

    // Getters y setters
}