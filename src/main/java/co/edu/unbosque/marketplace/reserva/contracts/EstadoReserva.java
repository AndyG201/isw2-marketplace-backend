package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;

/**
 * Estados de una reserva y las transiciones permitidas entre ellos. Es el único lugar donde se definen: el resto del
 * código usa estas constantes en vez de textos. El nombre de cada constante es el valor guardado en la base de datos
 * (columna {@code reserva.estado}); una prueba comprueba que coincidan con la restricción de la tabla.
 *
 * <pre>
 * PENDIENTE_APROBACION --aprobar--> PENDIENTE_PAGO --pago--> CONFIRMADA --fin de la estadía--> COMPLETADA
 *        |                              |                        |
 *        +--rechazar / vencer--> RECHAZADA   +--vencer--> EXPIRADA   +--cancelar (también desde los pendientes)--> CANCELADA
 * </pre>
 */
public enum EstadoReserva {

    // El orden coincide con la restricción chk_reserva_estado y con el del tablero de indicadores.
    PENDIENTE_PAGO,
    PENDIENTE_APROBACION,
    CONFIRMADA,
    RECHAZADA,
    CANCELADA,
    COMPLETADA,
    EXPIRADA;

    private static final Map<EstadoReserva, Set<EstadoReserva>> TRANSICIONES = new EnumMap<>(EstadoReserva.class);

    static {
        TRANSICIONES.put(PENDIENTE_APROBACION, EnumSet.of(PENDIENTE_PAGO, RECHAZADA, CANCELADA));
        TRANSICIONES.put(PENDIENTE_PAGO, EnumSet.of(CONFIRMADA, EXPIRADA, CANCELADA));
        TRANSICIONES.put(CONFIRMADA, EnumSet.of(COMPLETADA, CANCELADA));
        TRANSICIONES.put(RECHAZADA, EnumSet.noneOf(EstadoReserva.class));
        TRANSICIONES.put(CANCELADA, EnumSet.noneOf(EstadoReserva.class));
        TRANSICIONES.put(COMPLETADA, EnumSet.noneOf(EstadoReserva.class));
        TRANSICIONES.put(EXPIRADA, EnumSet.noneOf(EstadoReserva.class));
    }

    /** Reservas que siguen en curso: esperan aprobación o pago, o ya están confirmadas. */
    public static final Set<EstadoReserva> VIGENTES = EnumSet.of(PENDIENTE_PAGO, PENDIENTE_APROBACION, CONFIRMADA);

    /** Estados desde los que el huésped puede cancelar. */
    public static final Set<EstadoReserva> CANCELABLES = EnumSet.of(PENDIENTE_PAGO, PENDIENTE_APROBACION, CONFIRMADA);

    /** Estados cuyas noches están ocupadas sin importar la fecha. */
    public static final Set<EstadoReserva> QUE_OCUPAN_NOCHES = EnumSet.of(CONFIRMADA, COMPLETADA);

    /** Estados que retienen las noches solo mientras la retención no haya vencido. */
    public static final Set<EstadoReserva> CON_RETENCION = EnumSet.of(PENDIENTE_APROBACION, PENDIENTE_PAGO);

    /** Estados de una reserva que ya no debe tener noches asociadas. */
    public static final Set<EstadoReserva> QUE_LIBERAN_NOCHES = EnumSet.of(RECHAZADA, CANCELADA, EXPIRADA);

    public Set<EstadoReserva> transicionesPermitidas() {
        return Set.copyOf(TRANSICIONES.get(this));
    }

    public boolean puedePasarA(EstadoReserva destino) {
        return destino != null && TRANSICIONES.get(this).contains(destino);
    }

    /** Un estado final no admite más cambios. */
    public boolean esFinal() {
        return TRANSICIONES.get(this).isEmpty();
    }

    /**
     * Valida la transición y devuelve el estado destino.
     *
     * @throws ConflictoNegocioException si la reserva no puede pasar de este estado al indicado
     */
    public EstadoReserva transicionarA(EstadoReserva destino) {
        if (!puedePasarA(destino)) {
            throw new ConflictoNegocioException(
                    "Una reserva en estado " + this + " no puede pasar a " + destino + ".");
        }
        return destino;
    }

    /** Convierte el texto guardado en la base de datos. Un valor desconocido indica datos corruptos. */
    public static EstadoReserva desde(String texto) {
        if (texto != null) {
            for (EstadoReserva estado : values()) {
                if (estado.name().equals(texto)) {
                    return estado;
                }
            }
        }
        throw new FallaInternaException("La reserva tiene un estado desconocido: " + texto);
    }

    /** Nombres (los textos guardados en la base de datos) de un conjunto de estados. */
    public static Set<String> nombres(Set<EstadoReserva> estados) {
        return estados.stream().map(Enum::name).collect(Collectors.toUnmodifiableSet());
    }

    /** Nombres de todos los estados, en el orden de declaración. */
    public static java.util.List<String> nombres() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
