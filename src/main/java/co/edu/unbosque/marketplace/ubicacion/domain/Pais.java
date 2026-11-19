package co.edu.unbosque.marketplace.ubicacion.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pais")
public class Pais {
    @Id
    @Column(name = "id_pais", nullable = false)
    private UUID idPais;

    @Column(name = "codigo_iso2", nullable = false, unique = true, length = 2)
    private String codigoIso2;

    @Column(name = "nombre", nullable = false, unique = true, length = 120)
    private String nombre;

    protected Pais() { }

    public Pais(UUID idPais, String codigoIso2, String nombre) {
        this.idPais = idPais;
        this.codigoIso2 = codigoIso2;
        this.nombre = nombre;
    }

    public UUID getIdPais() { return idPais; }
    public String getCodigoIso2() { return codigoIso2; }
    public String getNombre() { return nombre; }
}
