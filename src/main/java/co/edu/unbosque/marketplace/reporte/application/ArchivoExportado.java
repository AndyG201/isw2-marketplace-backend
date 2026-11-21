package co.edu.unbosque.marketplace.reporte.application;

public final class ArchivoExportado {

    private final String nombre;
    private final String tipoContenido;
    private final byte[] contenido;

    public ArchivoExportado(String nombre, String tipoContenido, byte[] contenido) {
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.contenido = contenido.clone();
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public byte[] getContenido() {
        return contenido.clone();
    }
}
