package co.edu.unbosque.marketplace.ubicacion.application;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.ubicacion.application.dto.CiudadResponse;
import co.edu.unbosque.marketplace.ubicacion.application.dto.PaisResponse;
import co.edu.unbosque.marketplace.ubicacion.application.port.CatalogoUbicacionesExterno;
import co.edu.unbosque.marketplace.ubicacion.application.port.CatalogoUbicacionesExterno.PaisExterno;
import co.edu.unbosque.marketplace.ubicacion.application.port.CiudadRepository;
import co.edu.unbosque.marketplace.ubicacion.application.port.PaisRepository;
import co.edu.unbosque.marketplace.ubicacion.contracts.CiudadUbicacionInfo;
import co.edu.unbosque.marketplace.ubicacion.contracts.UbicacionRegistroQuery;
import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;
import co.edu.unbosque.marketplace.ubicacion.domain.Pais;

@Service
public class UbicacionService implements UbicacionRegistroQuery {
    private final PaisRepository paisRepository;
    private final CiudadRepository ciudadRepository;
    private final CatalogoUbicacionesExterno catalogoExterno;

    public UbicacionService(PaisRepository paisRepository, CiudadRepository ciudadRepository,
            CatalogoUbicacionesExterno catalogoExterno) {
        this.paisRepository = paisRepository;
        this.ciudadRepository = ciudadRepository;
        this.catalogoExterno = catalogoExterno;
    }

    @Transactional
    public List<PaisResponse> listarPaises() {
        if (paisRepository.count() == 0) sincronizarPaises();
        return paisRepository.findAllByOrderByNombreAsc().stream()
                .map(pais -> new PaisResponse(pais.getIdPais(), pais.getCodigoIso2(), pais.getNombre())).toList();
    }

    @Transactional
    public List<CiudadResponse> listarCiudades(UUID idPais) {
        Pais pais = paisRepository.findById(idPais)
                .orElseThrow(() -> new IllegalArgumentException("El país solicitado no existe."));
        if (!ciudadRepository.existsByPaisIdPais(idPais)) sincronizarCiudades(pais);
        return ciudadRepository.findAllByPaisIdPaisOrderByNombreAsc(idPais).stream()
                .map(ciudad -> new CiudadResponse(ciudad.getIdCiudad(), ciudad.getNombre())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCiudad(UUID idCiudad) {
        return idCiudad != null && ciudadRepository.existsByIdCiudad(idCiudad);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CiudadUbicacionInfo> consultarCiudad(UUID idCiudad) {
        if (idCiudad == null) {
            return Optional.empty();
        }
        return ciudadRepository.findById(idCiudad).map(ciudad -> new CiudadUbicacionInfo(
                ciudad.getNombre(), ciudad.getPais().getNombre(), ciudad.getPais().getCodigoIso2()));
    }

    private void sincronizarPaises() {
        List<PaisExterno> datos = catalogoExterno.paises();
        List<Pais> paises = new ArrayList<>();
        for (PaisExterno dato : datos) {
            if (paisRepository.findByCodigoIso2IgnoreCase(dato.codigoIso2()).isEmpty()) {
                UUID idPais = UUID.nameUUIDFromBytes(
                        ("iso:" + dato.codigoIso2()).getBytes(StandardCharsets.UTF_8));
                paises.add(new Pais(idPais, dato.codigoIso2(), dato.nombre()));
            }
        }
        paisRepository.saveAll(paises);
    }

    private void sincronizarCiudades(Pais pais) {
        Map<String, String> nombres = new LinkedHashMap<>();
        for (String nombre : catalogoExterno.ciudades(pais.getNombre())) {
            nombres.putIfAbsent(clave(nombre), nombre);
        }
        List<Ciudad> ciudades = nombres.values().stream()
                .map(nombre -> new Ciudad(UUID.nameUUIDFromBytes(
                        (pais.getIdPais() + ":" + clave(nombre)).getBytes(StandardCharsets.UTF_8)), pais, nombre))
                .toList();
        ciudadRepository.saveAll(ciudades);
    }

    private String clave(String nombre) {
        return Normalizer.normalize(nombre, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
                .trim().toLowerCase(Locale.ROOT);
    }
}
