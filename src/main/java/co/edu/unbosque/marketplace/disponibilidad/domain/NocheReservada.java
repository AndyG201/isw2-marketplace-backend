package co.edu.unbosque.marketplace.disponibilidad.domain;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "noche_reservada")
public class NocheReservada {

    @Id
    @Column(name = "id_noche", nullable = false)
    private UUID idNoche;

    @Column(name = "id_alojamiento", nullable = false)
    private UUID idAlojamiento;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    public NocheReservada() {
    }

	public NocheReservada(UUID idNoche, UUID idAlojamiento, LocalDate fecha) {
		super();
		this.idNoche = idNoche;
		this.idAlojamiento = idAlojamiento;
		this.fecha = fecha;
	}

	public UUID getIdNoche() {
		return idNoche;
	}

	public void setIdNoche(UUID idNoche) {
		this.idNoche = idNoche;
	}

	public UUID getIdAlojamiento() {
		return idAlojamiento;
	}

	public void setIdAlojamiento(UUID idAlojamiento) {
		this.idAlojamiento = idAlojamiento;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

   
}