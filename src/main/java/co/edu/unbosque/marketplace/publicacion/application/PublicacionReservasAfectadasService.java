
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.ReservaAfectadaResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.ReservasAfectadasResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioReservaInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioReservaQuery;

@Service
public class PublicacionReservasAfectadasService {

    private static final String BLOQUEADA = "BLOQUEADA";

    private final PublicacionRepository publicacionRepository;
    private final RevisionPublicacionRepository revisionRepository;
    private final CotizacionReservaQuery cotizacionQuery;
    private final ReservaAfectadaQuery reservaQuery;
    private final UsuarioReservaQuery usuarioQuery;

    public PublicacionReservasAfectadasService(
            PublicacionRepository publicacionRepository,
            RevisionPublicacionRepository revisionRepository,
            CotizacionReservaQuery cotizacionQuery,
            ReservaAfectadaQuery reservaQuery,
            UsuarioReservaQuery usuarioQuery) {

        this.publicacionRepository = publicacionRepository;
        this.revisionRepository = revisionRepository;
        this.cotizacionQuery = cotizacionQuery;
        this.reservaQuery = reservaQuery;
        this.usuarioQuery = usuarioQuery;
    }

    @Transactional(readOnly = true)
    public ReservasAfectadasResponse consultarReservasAfectadas(
            UUID idPublicacion) {

        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar el identificador de la publicación."
            );
        }

        Publicacion publicacion = publicacionRepository
                .findById(idPublicacion)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La publicación no existe."
                        ));

        if (!BLOQUEADA.equals(publicacion.getEstado())) {
            throw new IllegalStateException(
                    "Solamente puedes consultar reservas afectadas "
                    + "de publicaciones BLOQUEADAS."
            );
        }

        String motivoBloqueo = consultarMotivoBloqueo(idPublicacion);

        LocalDate hoy = LocalDate.now();

        List<CotizacionReservaInfo> cotizacionesFuturas =
                cotizacionQuery.consultarPorPublicacion(idPublicacion)
                        .stream()
                        .filter(cotizacion ->
                                cotizacion.fechaEntrada() != null
                                && cotizacion.fechaSalida() != null
                                && !cotizacion.fechaEntrada().isBefore(hoy)
                                && cotizacion.fechaSalida().isAfter(hoy)
                        )
                        .toList();

        if (cotizacionesFuturas.isEmpty()) {
            return respuestaSinReservas();
        }

        Map<UUID, CotizacionReservaInfo> cotizacionesPorId =
                new HashMap<>();

        for (CotizacionReservaInfo cotizacion : cotizacionesFuturas) {
            cotizacionesPorId.put(
                    cotizacion.idCotizacion(),
                    cotizacion
            );
        }

        List<ReservaAfectadaInfo> reservas =
                reservaQuery.consultarPorCotizaciones(
                        cotizacionesPorId.keySet()
                );

        if (reservas.isEmpty()) {
            return respuestaSinReservas();
        }

        List<ReservaAfectadaResponse> resultado = new ArrayList<>();

        for (ReservaAfectadaInfo reserva : reservas) {

            CotizacionReservaInfo cotizacion =
                    cotizacionesPorId.get(reserva.idCotizacion());

            if (cotizacion == null) {
                continue;
            }

            UsuarioReservaInfo huesped =
                    usuarioQuery.consultarDatosHuesped(
                            reserva.idHuesped()
                    ).orElse(new UsuarioReservaInfo(
                            reserva.idHuesped(),
                            "Huésped no disponible"
                    ));

            ReservaAfectadaResponse respuesta =
                    new ReservaAfectadaResponse(
                            reserva.idReserva(),
                            publicacion.getIdPublicacion(),
                            publicacion.getTitulo(),
                            publicacion.getIdAlojamiento(),
                            huesped.nombreCompleto(),
                            cotizacion.fechaEntrada(),
                            cotizacion.fechaSalida(),
                            reserva.estado(),
                            motivoBloqueo
                    );

            resultado.add(respuesta);
        }

        if (resultado.isEmpty()) {
            return respuestaSinReservas();
        }

        return new ReservasAfectadasResponse(
                "Reservas futuras afectadas consultadas correctamente.",
                resultado
        );
    }

    private String consultarMotivoBloqueo(UUID idPublicacion) {

        return revisionRepository
                .findByIdPublicacionOrderByFechaRevisionDesc(idPublicacion)
                .stream()
                .filter(revision ->
                        "BLOQUEAR_PUBLICACION".equals(
                                revision.getAccion()
                        ))
                .findFirst()
                .map(RevisionPublicacion::getMotivo)
                .filter(motivo ->
                        motivo != null && !motivo.isBlank())
                .orElse(
                        "Motivo de bloqueo no registrado en el historial."
                );
    }

    private ReservasAfectadasResponse respuestaSinReservas() {

        return new ReservasAfectadasResponse(
                "No existen reservas futuras afectadas por el bloqueo.",
                List.of()
        );
    }
}
