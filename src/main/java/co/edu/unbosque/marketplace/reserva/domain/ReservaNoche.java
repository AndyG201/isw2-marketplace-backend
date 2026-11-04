package co.edu.unbosque.marketplace.reserva.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reserva_noche")
public class ReservaNoche {

    @Id
    @Column(name = "id_noche", nullable = false)
    private UUID idNoche;

    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    public ReservaNoche() {
    }

	public ReservaNoche(UUID idNoche, UUID idReserva) {
		super();
		this.idNoche = idNoche;
		this.idReserva = idReserva;
	}

	public UUID getIdNoche() {
		return idNoche;
	}

	public void setIdNoche(UUID idNoche) {
		this.idNoche = idNoche;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

    
}