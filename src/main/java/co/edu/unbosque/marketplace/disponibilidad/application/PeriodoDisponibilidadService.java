package co.edu.unbosque.marketplace.disponibilidad.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.disponibilidad.api.request.PeriodoDisponibilidadRequest;
import co.edu.unbosque.marketplace.disponibilidad.api.response.PeriodoDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.NocheReservadaRepository;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class PeriodoDisponibilidadService {

    private final PeriodoDisponibilidadRepository periodoRepository;
    private final NocheReservadaRepository nocheReservadaRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final UsuarioPublicacionQuery usuarioQuery;

    public PeriodoDisponibilidadService(
            PeriodoDisponibilidadRepository periodoRepository,
            NocheReservadaRepository nocheReservadaRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            UsuarioPublicacionQuery usuarioQuery) {

        this.periodoRepository = periodoRepository;
        this.nocheReservadaRepository = nocheReservadaRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.usuarioQuery = usuarioQuery;
    }

    @Transactional
    public PeriodoDisponibilidadResponse registrarPeriodo(
            UUID idAnfitrion,
            UUID idAlojamiento,
            PeriodoDisponibilidadRequest solicitud) {

        validarIdentificadores(idAnfitrion, idAlojamiento);

        validarSolicitud(solicitud);

        validarPropietario(idAnfitrion, idAlojamiento);

        validarSinReservas(
                idAlojamiento,
                solicitud.getFechaInicio(),
                solicitud.getFechaFin()
        );

        validarSinSolapamiento(
                idAlojamiento,
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                null
        );

        PeriodoDisponibilidad periodo =
                new PeriodoDisponibilidad(
                        UUID.randomUUID(),
                        idAlojamiento,
                        solicitud.getFechaInicio(),
                        solicitud.getFechaFin(),
                        true,
                        OffsetDateTime.now()
                );

        PeriodoDisponibilidad guardado =
                periodoRepository.save(periodo);

        return convertirRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public List<PeriodoDisponibilidadResponse> consultarPeriodos(
            UUID idAnfitrion,
            UUID idAlojamiento) {

        validarIdentificadores(idAnfitrion, idAlojamiento);

        validarPropietario(idAnfitrion, idAlojamiento);

        return periodoRepository
                .findByIdAlojamientoOrderByFechaInicioAsc(idAlojamiento)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional
    public PeriodoDisponibilidadResponse actualizarPeriodo(
            UUID idAnfitrion,
            UUID idAlojamiento,
            UUID idPeriodo,
            PeriodoDisponibilidadRequest solicitud) {

        validarIdentificadores(idAnfitrion, idAlojamiento);

        if (idPeriodo == null) {
            throw new IllegalArgumentException(
                    "El identificador del periodo es obligatorio."
            );
        }

        validarSolicitud(solicitud);

        validarPropietario(idAnfitrion, idAlojamiento);

        PeriodoDisponibilidad periodo = obtenerPeriodo(
                idPeriodo,
                idAlojamiento
        );

        // No se pueden modificar periodos que contienen
        // noches asociadas a reservas.

        validarSinReservas(
                idAlojamiento,
                periodo.getFechaInicio(),
                periodo.getFechaFin()
        );

        // Tampoco se puede extender el periodo sobre
        // noches ya retenidas o reservadas.

        validarSinReservas(
                idAlojamiento,
                solicitud.getFechaInicio(),
                solicitud.getFechaFin()
        );

        if (periodo.isHabilitado()) {

            validarSinSolapamiento(
                    idAlojamiento,
                    solicitud.getFechaInicio(),
                    solicitud.getFechaFin(),
                    idPeriodo
            );
        }

        periodo.setFechaInicio(solicitud.getFechaInicio());
        periodo.setFechaFin(solicitud.getFechaFin());

        PeriodoDisponibilidad actualizado =
                periodoRepository.save(periodo);

        return convertirRespuesta(actualizado);
    }

    @Transactional
    public PeriodoDisponibilidadResponse deshabilitarPeriodo(
            UUID idAnfitrion,
            UUID idAlojamiento,
            UUID idPeriodo) {

        validarIdentificadores(idAnfitrion, idAlojamiento);

        if (idPeriodo == null) {
            throw new IllegalArgumentException(
                    "El identificador del periodo es obligatorio."
            );
        }

        validarPropietario(idAnfitrion, idAlojamiento);

        PeriodoDisponibilidad periodo = obtenerPeriodo(
                idPeriodo,
                idAlojamiento
        );

        if (!periodo.isHabilitado()) {
            return convertirRespuesta(periodo);
        }

        validarSinReservas(
                idAlojamiento,
                periodo.getFechaInicio(),
                periodo.getFechaFin()
        );

        // Se deshabilita lógicamente para conservar el registro.

        periodo.setHabilitado(false);

        PeriodoDisponibilidad actualizado =
                periodoRepository.save(periodo);

        return convertirRespuesta(actualizado);
    }

    private void validarIdentificadores(
            UUID idAnfitrion,
            UUID idAlojamiento) {

        if (idAnfitrion == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para administrar la disponibilidad."
            );
        }

        if (idAlojamiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del alojamiento es obligatorio."
            );
        }
    }

    private void validarSolicitud(
            PeriodoDisponibilidadRequest solicitud) {

        if (solicitud == null
                || solicitud.getFechaInicio() == null
                || solicitud.getFechaFin() == null) {

            throw new IllegalArgumentException(
                    "Debes proporcionar las fechas de inicio y fin."
            );
        }

        if (!solicitud.getFechaFin().isAfter(
                solicitud.getFechaInicio())) {

            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la fecha de llegada."
            );
        }
    }

    private void validarPropietario(
            UUID idAnfitrion,
            UUID idAlojamiento) {

        UsuarioPublicacionInfo anfitrion = usuarioQuery
                .consultarParaPublicacion(idAnfitrion)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "No tienes permisos para administrar periodos."
                        ));

        if (!anfitrion.esAnfitrion()
                || !anfitrion.cuentaHabilitadaParaPublicar()) {

            throw new AccessDeniedException(
                    "Solamente un anfitrión con cuenta habilitada puede administrar periodos."
            );
        }

        UUID idPropietario = alojamientoQuery
                .consultarIdAnfitrion(idAlojamiento)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "El alojamiento no existe."
                        ));

        if (!idAnfitrion.equals(idPropietario)) {

            throw new AccessDeniedException(
                    "No puedes administrar la disponibilidad de un alojamiento que no te pertenece."
            );
        }
    }

    private PeriodoDisponibilidad obtenerPeriodo(
            UUID idPeriodo,
            UUID idAlojamiento) {

        return periodoRepository
                .findByIdPeriodoAndIdAlojamiento(
                        idPeriodo,
                        idAlojamiento
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "El periodo de disponibilidad no existe para este alojamiento."
                        ));
    }

    private void validarSinReservas(
            UUID idAlojamiento,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin) {

        boolean ocupado =
                nocheReservadaRepository.existeNocheOcupadaEnRango(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
                );

        if (ocupado) {

            throw new IllegalStateException(
                    "El periodo contiene noches asociadas a reservas vigentes o confirmadas y no puede modificarse."
            );
        }
    }

    private void validarSinSolapamiento(
            UUID idAlojamiento,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            UUID idPeriodoExcluir) {

        boolean existeSolapamiento;

        if (idPeriodoExcluir == null) {

            existeSolapamiento =
                    periodoRepository.existeSolapamientoHabilitado(
                            idAlojamiento,
                            fechaInicio,
                            fechaFin
                    );

        } else {

            existeSolapamiento =
                    periodoRepository.existeSolapamientoExcluyendoPeriodo(
                            idAlojamiento,
                            idPeriodoExcluir,
                            fechaInicio,
                            fechaFin
                    );
        }

        if (existeSolapamiento) {

            throw new IllegalStateException(
                    "El periodo solicitado se superpone con otro periodo disponible del alojamiento."
            );
        }
    }

    private PeriodoDisponibilidadResponse convertirRespuesta(
            PeriodoDisponibilidad periodo) {

        return new PeriodoDisponibilidadResponse(
                periodo.getIdPeriodo(),
                periodo.getIdAlojamiento(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                periodo.isHabilitado(),
                periodo.getFechaCreacion()
        );
    }
}
