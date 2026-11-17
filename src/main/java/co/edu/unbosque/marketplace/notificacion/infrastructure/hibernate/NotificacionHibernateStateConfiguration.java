package co.edu.unbosque.marketplace.notificacion.infrastructure.hibernate;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NotificacionHibernateStateConfiguration implements SmartInitializingSingleton {

    private final EntityManagerFactory entityManagerFactory;
    private final ApplicationEventPublisher eventos;

    public NotificacionHibernateStateConfiguration(
            EntityManagerFactory entityManagerFactory,
            ApplicationEventPublisher eventos) {
        this.entityManagerFactory = entityManagerFactory;
        this.eventos = eventos;
    }

    @Override
    public void afterSingletonsInstantiated() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        EventListenerRegistry registry = sessionFactory.getSessionFactoryOptions()
                .getServiceRegistry()
                .getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_UPDATE, new NotificacionHibernateStateListener(eventos));
    }
}
