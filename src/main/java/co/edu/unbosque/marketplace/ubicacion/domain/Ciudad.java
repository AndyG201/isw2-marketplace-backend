package co.edu.unbosque.marketplace.ubicacion.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ciudad", uniqueConstraints = @UniqueConstraint(name = "uq_ciudad_pais_nombre", columnNames = {"id_pais", "nombre"}))
public class Ciudad {
    @Id
    @Column(name = "id_ciudad", nullable = false)
    private UUID idCiudad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    protected Ciudad() { }

    public Ciudad(UUID idCiudad, Pais pais, String nombre) {
        this.idCiudad = idCiudad;
        this.pais = pais;
        this.nombre = nombre;
    }

    public UUID getIdCiudad() { return idCiudad; }
    public Pais getPais() { return pais; }
    public String getNombre() { return nombre; }
}
