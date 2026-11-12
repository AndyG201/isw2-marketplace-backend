package co.edu.unbosque.marketplace.disponibilidad.application;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.disponibilidad.api.response.CalendarioDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.api.response.ConsultaDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.api.response.PeriodoDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.domain.NocheReservada;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.NocheReservadaRepository;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class CalendarioDisponibilidadService {

    private final PeriodoDisponibilidadRepository periodoRepository;
    private final NocheReservadaRepository nocheReservadaRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final UsuarioPublicacionQuery usuarioQuery;
    private final PublicacionDisponibilidadQuery publicacionQuery;

    public CalendarioDisponibilidadService(
            PeriodoDisponibilidadRepository periodoRepository,
            NocheReservadaRepository nocheReservadaRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            UsuarioPublicacionQuery usuarioQuery,
            PublicacionDisponibilidadQuery publicacionQuery) {

        this.periodoRepository = periodoRepository;
        this.nocheReservadaRepository = nocheReservadaRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.usuarioQuery = usuarioQuery;
        this.publicacionQuery = publicacionQuery;
    }

    @Transactional(readOnly = true)
    public ConsultaDisponibilidadResponse consultarPorFechas(
            UUID idAlojamiento,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        validarRango(idAlojamiento, fechaInicio, fechaFin);

        if (!publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento)) {

            throw new NoSuchElementException(
                    "El alojamiento no tiene una publicación activa y consultable."
            );
        }

        List<PeriodoDisponibilidad> periodosDisponibles =
                periodoRepository
                        .findByIdAlojamientoOrderByFechaInicioAsc(
                                idAlojamiento
                        )
                        .stream()
                        .filter(PeriodoDisponibilidad::isHabilitado)
                        .filter(periodo ->
                                seCruzaConRango(
                                        periodo,
                                        fechaInicio,
                                        fechaFin
                                )
                        )
                        .toList();

        List<LocalDate> nochesOcupadas =
                nocheReservadaRepository
                        .consultarNochesOcupadasEnRango(
                                idAlojamiento,
                                fechaInicio,
                                fechaFin
                        )
                        .stream()
                        .map(NocheReservada::getFecha)
                        .distinct()
                        .sorted()
                        .toList();

        boolean cubierto = rangoCubiertoPorPeriodos(
                fechaInicio,
                fechaFin,
                periodosDisponibles
        );

        boolean disponible = cubierto && nochesOcupadas.isEmpty();

        return new ConsultaDisponibilidadResponse(
                idAlojamiento,
                fechaInicio,
                fechaFin,
                disponible,
                cubierto,
                nochesOcupadas
        );
    }

    @Transactional(readOnly = true)
    public CalendarioDisponibilidadResponse consultarCalendarioCompleto(
            UUID idAnfitrion,
            UUID idAlojamiento) {

        validarIdentificadoresCalendario(
                idAnfitrion,
                idAlojamiento
        );

        validarPropietario(idAnfitrion, idAlojamiento);

        List<PeriodoDisponibilidadResponse> periodos =
                periodoRepository
                        .findByIdAlojamientoOrderByFechaInicioAsc(
                                idAlojamiento
                        )
                        .stream()
                        .map(this::convertirPeriodo)
                        .toList();

        List<LocalDate> nochesOcupadas =
                nocheReservadaRepository
                        .consultarTodasLasNochesOcupadas(idAlojamiento)
                        .stream()
                        .map(NocheReservada::getFecha)
                        .distinct()
                        .sorted()
                        .toList();

        return new CalendarioDisponibilidadResponse(
                idAlojamiento,
                periodos,
                nochesOcupadas
        );
    }

    private void validarRango(
            UUID idAlojamiento,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (idAlojamiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del alojamiento es obligatorio."
            );
        }

        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException(
                    "Las fechas de inicio y fin son obligatorias."
            );
        }

        if (!fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la fecha de llegada."
            );
        }
    }

    private void validarIdentificadoresCalendario(
            UUID idAnfitrion,
            UUID idAlojamiento) {

        if (idAnfitrion == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para consultar el calendario completo."
            );
        }

        if (idAlojamiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del alojamiento es obligatorio."
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
                                "No tienes permisos para consultar este calendario."
                        )
                );

        if (!anfitrion.esAnfitrion()
                || !anfitrion.cuentaHabilitadaParaPublicar()) {

            throw new AccessDeniedException(
                    "Solo un anfitrión con cuenta habilitada puede consultar el calendario completo."
            );
        }

        UUID idPropietario = alojamientoQuery
                .consultarIdAnfitrion(idAlojamiento)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "El alojamiento no existe."
                        )
                );

        if (!idAnfitrion.equals(idPropietario)) {
            throw new AccessDeniedException(
                    "No puedes consultar el calendario de un alojamiento que no te pertenece."
            );
        }
    }

    private boolean seCruzaConRango(
            PeriodoDisponibilidad periodo,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        return periodo.getFechaInicio().isBefore(fechaFin)
                && periodo.getFechaFin().isAfter(fechaInicio);
    }

    private boolean rangoCubiertoPorPeriodos(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<PeriodoDisponibilidad> periodos) {

        LocalDate cursor = fechaInicio;

        for (PeriodoDisponibilidad periodo : periodos) {

            if (!periodo.getFechaFin().isAfter(cursor)) {
                continue;
            }

            if (periodo.getFechaInicio().isAfter(cursor)) {
                return false;
            }

            if (periodo.getFechaFin().isAfter(cursor)) {
                cursor = periodo.getFechaFin();
            }

            if (!cursor.isBefore(fechaFin)) {
                return true;
            }
        }

        return false;
    }

    private PeriodoDisponibilidadResponse convertirPeriodo(
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
