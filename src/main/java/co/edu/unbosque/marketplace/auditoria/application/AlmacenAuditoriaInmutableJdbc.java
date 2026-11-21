package co.edu.unbosque.marketplace.auditoria.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@Component
@Profile("worker")
public class AlmacenAuditoriaInmutableJdbc {

    private static final String INSERTAR = """
            INSERT INTO public.evento_auditoria (
                id_evento, id_usuario, fecha_evento, accion, resultado,
                entidad_afectada, id_entidad_afectada, detalle, id_correlacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?)
            ON CONFLICT (id_evento) DO NOTHING
            """;

    private final String url;
    private final String usuario;
    private final String contrasena;

    public AlmacenAuditoriaInmutableJdbc(
            @Value("$" + "{app.auditoria.almacen.url:}") String url,
            @Value("$" + "{app.auditoria.almacen.username:}") String usuario,
            @Value("$" + "{app.auditoria.almacen.password:}") String contrasena) {
        this.url = url;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public void anexar(BandejaSalidaAuditoria evento) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "Configura APP_AUDITORIA_ALMACEN_URL, APP_AUDITORIA_ALMACEN_USERNAME y APP_AUDITORIA_ALMACEN_PASSWORD para el worker.");
        }

        try (Connection conexion = DriverManager.getConnection(
                url,
                usuario == null ? "" : usuario,
                contrasena == null ? "" : contrasena);
                PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setObject(1, evento.getIdEvento());
            if (evento.getIdUsuario() == null) {
                sentencia.setNull(2, Types.OTHER);
            } else {
                sentencia.setObject(2, evento.getIdUsuario());
            }
            sentencia.setObject(3, evento.getFechaEvento());
            sentencia.setString(4, evento.getAccion());
            sentencia.setString(5, evento.getResultado());
            sentencia.setString(6, evento.getEntidadAfectada());
            if (evento.getIdEntidadAfectada() == null) {
                sentencia.setNull(7, Types.OTHER);
            } else {
                sentencia.setObject(7, evento.getIdEntidadAfectada());
            }
            if (evento.getDetalle() == null) {
                sentencia.setNull(8, Types.VARCHAR);
            } else {
                sentencia.setString(8, evento.getDetalle());
            }
            sentencia.setString(9, evento.getIdCorrelacion());
            sentencia.executeUpdate();
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "No fue posible copiar el evento al almacén de auditoría inmutable.",
                    error);
        }
    }
}