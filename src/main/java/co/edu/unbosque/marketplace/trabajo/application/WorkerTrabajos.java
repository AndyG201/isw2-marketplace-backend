package co.edu.unbosque.marketplace.trabajo.application;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;
import co.edu.unbosque.marketplace.trabajo.ProcesadorTrabajo;

@Component
@Profile("worker")
public class WorkerTrabajos {

    private final ColaTrabajosRedis cola;
    private final List<ProcesadorTrabajo> procesadores;

    public WorkerTrabajos(
            ColaTrabajosRedis cola,
            List<ProcesadorTrabajo> procesadores) {
        this.cola = cola;
        this.procesadores = List.copyOf(procesadores);
    }

    @Scheduled(fixedDelayString = "${app.worker.intervalo-ms:250}")
    public void procesarSiguiente() {
        cola.tomarSiguiente().ifPresent(trabajo -> {
            ProcesadorTrabajo procesador = procesadores.stream()
                    .filter(candidato -> candidato.soporta(trabajo.tipo()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe un procesador para el trabajo "
                                    + trabajo.tipo()));

            procesador.procesar(
                    trabajo.tipo(),
                    trabajo.identificador());
        });
    }
}