package co.edu.unbosque.marketplace.publicacion.contracts;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;

/**
 * Estados de una publicación y las transiciones permitidas entre ellos. Es el único lugar donde se definen: el resto
 * del código usa estas constantes en vez de textos. El nombre de cada constante es el valor guardado en la base de datos
 * (columna {@code publicacion.estado}); una prueba comprueba que coincidan con la restricción de la tabla.
 *
 * <pre>
 * BORRADOR --enviar a revisión--> PENDIENTE_REVISION --aprobar--> ACTIVA <--pausar / reactivar--> PAUSADA
 *    ^                                  |     ^                    |                                 |
 *    +------------rechazar--------------+     +---editar (cambios sensibles)--- ACTIVA o PAUSADA ----+
 *
 * Un administrador puede BLOQUEAR cualquier publicación; al desbloquearla queda PAUSADA.
 * </pre>
 *
 * Al aprobar una edición, la publicación vuelve al estado en que estaba (ACTIVA o PAUSADA).
 */
public enum EstadoPublicacion {

    BORRADOR,
    PENDIENTE_REVISION,
    ACTIVA,
    PAUSADA,
    BLOQUEADA;

    private static final Map<EstadoPublicacion, Set<EstadoPublicacion>> TRANSICIONES =
            new EnumMap<>(EstadoPublicacion.class);

    static {
        TRANSICIONES.put(BORRADOR, EnumSet.of(PENDIENTE_REVISION, BLOQUEADA));
        TRANSICIONES.put(PENDIENTE_REVISION, EnumSet.of(ACTIVA, PAUSADA, BORRADOR, BLOQUEADA));
        TRANSICIONES.put(ACTIVA, EnumSet.of(PAUSADA, PENDIENTE_REVISION, BLOQUEADA));
        TRANSICIONES.put(PAUSADA, EnumSet.of(ACTIVA, PENDIENTE_REVISION, BLOQUEADA));
        TRANSICIONES.put(BLOQUEADA, EnumSet.of(PAUSADA));
    }

    /** Estados que ya pasaron por una revisión administrativa: sus cambios sensibles vuelven a revisión. */
    public static final Set<EstadoPublicacion> APROBADAS = EnumSet.of(ACTIVA, PAUSADA);

    /** Estados en los que el anfitrión no puede editar. */
    public static final Set<EstadoPublicacion> SIN_EDICION = EnumSet.of(PENDIENTE_REVISION, BLOQUEADA);

    /** Nombres (los textos guardados en la base de datos) de un conjunto de estados. */
    public static Set<String> nombres(Set<EstadoPublicacion> estados) {
        return estados.stream().map(Enum::name).collect(Collectors.toUnmodifiableSet());
    }

    public Set<EstadoPublicacion> transicionesPermitidas() {
        return Set.copyOf(TRANSICIONES.get(this));
    }

    public boolean puedePasarA(EstadoPublicacion destino) {
        return destino != null && TRANSICIONES.get(this).contains(destino);
    }

    /**
     * Valida la transición y devuelve el estado destino.
     *
     * @throws ConflictoNegocioException si la publicación no puede pasar de este estado al indicado
     */
    public EstadoPublicacion transicionarA(EstadoPublicacion destino) {
        if (!puedePasarA(destino)) {
            throw new ConflictoNegocioException(
                    "Una publicación en estado " + this + " no puede pasar a " + destino + ".");
        }
        return destino;
    }

    /** Convierte el texto guardado en la base de datos. Un valor desconocido indica datos corruptos. */
    public static EstadoPublicacion desde(String texto) {
        if (texto != null) {
            for (EstadoPublicacion estado : values()) {
                if (estado.name().equals(texto)) {
                    return estado;
                }
            }
        }
        throw new FallaInternaException("La publicación tiene un estado desconocido: " + texto);
    }
}
