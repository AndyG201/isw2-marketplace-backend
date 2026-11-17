package co.edu.unbosque.marketplace.notificacion.infrastructure.hibernate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.unbosque.marketplace.notificacion.application.EstadoMarketplaceCambioEvent;

public class NotificacionHibernateStateListener implements PostUpdateEventListener {

    private static final String RESERVA = "RESERVA";
    private static final String PUBLICACION = "PUBLICACION";

    private final ApplicationEventPublisher eventos;

    public NotificacionHibernateStateListener(ApplicationEventPublisher eventos) {
        this.eventos = eventos;
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        String nombreEntidad = event.getPersister().getEntityName();
        String entidad = nombreEntidad.endsWith(".Reserva")
                ? RESERVA
                : nombreEntidad.endsWith(".Publicacion") ? PUBLICACION : null;
        if (entidad == null) {
            return;
        }

        String[] propiedades = event.getPersister().getPropertyNames();
        int indiceEstado = Arrays.asList(propiedades).indexOf("estado");
        Object[] anterior = event.getOldState();
        Object[] actual = event.getState();
        if (indiceEstado < 0 || anterior == null || actual == null
                || indiceEstado >= anterior.length || indiceEstado >= actual.length) {
            return;
        }

        String estadoAnterior = Objects.toString(anterior[indiceEstado], null);
        String estadoNuevo = Objects.toString(actual[indiceEstado], null);
        if (estadoAnterior == null || estadoNuevo == null || estadoAnterior.equals(estadoNuevo)) {
            return;
        }

        Object identificador = event.getPersister().getIdentifier(event.getEntity(), event.getSession());
        if (!(identificador instanceof UUID idEntidad)) {
            return;
        }

        eventos.publishEvent(new EstadoMarketplaceCambioEvent(
                entidad,
                idEntidad,
                estadoAnterior,
                estadoNuevo,
                OffsetDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}
