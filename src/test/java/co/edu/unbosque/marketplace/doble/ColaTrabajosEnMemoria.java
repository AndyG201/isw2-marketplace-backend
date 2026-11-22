package co.edu.unbosque.marketplace.doble;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

/**
 * Versión en memoria de la cola de trabajos, para pruebas y desarrollo sin Redis. Igual que la de Redis: sale primero
 * el de menor número de prioridad, a igual prioridad el más antiguo, y un trabajo ya pendiente no se encola dos veces.
 */
public class ColaTrabajosEnMemoria implements ColaTrabajos {

    private record Entrada(Trabajo trabajo, long orden) {
    }

    private final PriorityQueue<Entrada> cola = new PriorityQueue<>(
            Comparator.comparingInt((Entrada e) -> e.trabajo().tipo().getPrioridad())
                    .thenComparingLong(Entrada::orden));
    private final Set<Trabajo> pendientes = new HashSet<>();
    private long contador;

    @Override
    public synchronized void encolar(TipoTrabajo tipo, String identificador) {
        Trabajo trabajo = new Trabajo(tipo, identificador);
        if (pendientes.add(trabajo)) {
            cola.add(new Entrada(trabajo, contador++));
        }
    }

    @Override
    public synchronized Optional<Trabajo> tomarSiguiente() {
        Entrada siguiente = cola.poll();
        if (siguiente == null) {
            return Optional.empty();
        }
        pendientes.remove(siguiente.trabajo());
        return Optional.of(siguiente.trabajo());
    }
}
