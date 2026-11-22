package co.edu.unbosque.marketplace.doble;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;

/**
 * Versión en memoria del almacén de datos temporales, para pruebas y desarrollo sin Redis. El reloj se puede inyectar
 * para comprobar vigencias sin esperar.
 */
public class AlmacenTemporalEnMemoria implements AlmacenTemporal {

    private record Valor(String texto, Instant expira) {
    }

    private final Map<String, Valor> datos = new HashMap<>();
    private final Supplier<Instant> reloj;

    public AlmacenTemporalEnMemoria() {
        this(Instant::now);
    }

    public AlmacenTemporalEnMemoria(Supplier<Instant> reloj) {
        this.reloj = reloj;
    }

    @Override
    public synchronized void guardar(String clave, String valor, Duration vigencia) {
        datos.put(clave, new Valor(valor, reloj.get().plus(vigencia)));
    }

    @Override
    public synchronized String obtener(String clave) {
        Valor valor = vigente(clave);
        return valor == null ? null : valor.texto();
    }

    @Override
    public synchronized boolean guardarSiAusente(String clave, String valor, Duration vigencia) {
        if (vigente(clave) != null) {
            return false;
        }
        guardar(clave, valor, vigencia);
        return true;
    }

    @Override
    public synchronized void eliminar(String clave) {
        datos.remove(clave);
    }

    @Override
    public synchronized Long incrementar(String clave) {
        return sumar(clave, 1);
    }

    @Override
    public synchronized Long decrementar(String clave) {
        return sumar(clave, -1);
    }

    @Override
    public synchronized void asignarVigencia(String clave, Duration vigencia) {
        Valor valor = vigente(clave);
        if (valor != null) {
            datos.put(clave, new Valor(valor.texto(), reloj.get().plus(vigencia)));
        }
    }

    @Override
    public synchronized Long segundosRestantes(String clave) {
        Valor valor = vigente(clave);
        if (valor == null || valor.expira() == null) {
            return null;
        }
        return Math.max(0, Duration.between(reloj.get(), valor.expira()).toSeconds());
    }

    private Long sumar(String clave, long delta) {
        Valor valor = vigente(clave);
        long nuevo = (valor == null ? 0 : Long.parseLong(valor.texto())) + delta;
        // Como en Redis, cambiar un contador existente no altera su vigencia.
        datos.put(clave, new Valor(Long.toString(nuevo), valor == null ? null : valor.expira()));
        return nuevo;
    }

    private Valor vigente(String clave) {
        Valor valor = datos.get(clave);
        if (valor != null && valor.expira() != null && !reloj.get().isBefore(valor.expira())) {
            datos.remove(clave);
            return null;
        }
        return valor;
    }
}
