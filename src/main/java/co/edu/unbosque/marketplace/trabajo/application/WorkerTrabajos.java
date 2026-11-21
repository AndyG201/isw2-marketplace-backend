package co.edu.unbosque.marketplace.trabajo.application;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.ProcesadorTrabajo;

@Component
@Profile("worker")
public class WorkerTrabajos {

    private final ColaTrabajos cola;
    private final List<ProcesadorTrabajo> procesadores;

    public WorkerTrabajos(
            ColaTrabajos cola,
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
                    .orElseThrow(() -> new FallaInternaException(
                            "No existe un procesador para el trabajo "
                                    + trabajo.tipo()));

            procesador.procesar(
                    trabajo.tipo(),
                    trabajo.identificador());
        });
    }
}