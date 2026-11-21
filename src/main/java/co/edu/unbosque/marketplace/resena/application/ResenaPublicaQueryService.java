package co.edu.unbosque.marketplace.resena.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.resena.api.response.ResenaPublicacionItemResponse;
import co.edu.unbosque.marketplace.resena.api.response.ResenasPublicacionResponse;
import co.edu.unbosque.marketplace.resena.domain.Resena;
import co.edu.unbosque.marketplace.resena.infrastructure.persistence.ResenaPublicaQueryRepository;

@Service
public class ResenaPublicaQueryService {

    private final ResenaPublicaQueryRepository resenas;

    public ResenaPublicaQueryService(ResenaPublicaQueryRepository resenas) {
        this.resenas = resenas;
    }

    @Transactional(readOnly = true)
    public ResenasPublicacionResponse consultar(UUID idPublicacion) {
        if (idPublicacion == null) {
            throw new DatosInvalidosException("El identificador de publicación es obligatorio.");
        }
        if (!resenas.existePublicacion(idPublicacion)) {
            throw new RecursoNoEncontradoException("La publicación no existe.");
        }

        List<Resena> resultado = resenas.consultarVisibles(idPublicacion);
        List<ResenaPublicacionItemResponse> items = resultado.stream()
                .map(resena -> new ResenaPublicacionItemResponse(
                        resena.getIdResena(),
                        resena.getCalificacion(),
                        resena.getComentario(),
                        resena.getFechaPublicacion()))
                .toList();
        BigDecimal promedio = resultado.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(resultado.stream().mapToInt(Resena::getCalificacion).sum())
                        .divide(BigDecimal.valueOf(resultado.size()), 2, RoundingMode.HALF_UP);
        String mensaje = resultado.isEmpty() ? "Esta publicación aún no tiene reseñas visibles." : null;

        return new ResenasPublicacionResponse(idPublicacion, items, items.size(), promedio, mensaje);
    }
}
