package grupo.diseno.mindu.scheduler;

import grupo.diseno.mindu.model.*;
import grupo.diseno.mindu.repository.*;
import grupo.diseno.mindu.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecordatorioScheduler {

    private final CitaRepository citaRepository;
    private final EstudianteRepository estudianteRepository;
    private final RegistroEmocionalRepository registroEmocionalRepository;
    private final NotificacionRepository notificacionRepository;
    private final NotificacionService notificacionService;

    // HU019 — corre cada 15 min, avisa citas dentro de las próximas 24h
    @Scheduled(cron = "0 */15 * * * *")
    public void enviarRecordatoriosDeCitas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(24);

        List<Cita> candidatas = citaRepository.findByEstadoInAndRecordatorioEnviadoFalseAndFechaGreaterThanEqual(
                List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA), ahora.toLocalDate());

        for (Cita cita : candidatas) {
            LocalDateTime momentoCita = LocalDateTime.of(cita.getFecha(), cita.getHora());
            if (!momentoCita.isBefore(ahora) && !momentoCita.isAfter(limite)) {
                String mensaje = "Tienes una cita con " + cita.getPsicologo().getNombre() + " "
                        + cita.getPsicologo().getApellido() + " el " + cita.getFecha()
                        + " a las " + cita.getHora() + ".";

                notificacionService.crear(cita.getEstudiante(), TipoNotificacion.RECORDATORIO_CITA,
                        "Recordatorio de cita", mensaje, cita.getId());

                cita.setRecordatorioEnviado(true);
                citaRepository.save(cita);
            }
        }
    }

    // HU020 — corre todos los días 8:00 a.m.
    @Scheduled(cron = "0 0 8 * * *")
    public void generarAlertasBienestar() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        for (Estudiante estudiante : estudianteRepository.findByActivoTrue()) {
            boolean yaNotificadoHoy = notificacionRepository.existsByUsuarioIdAndTipoAndCreadoEnBetween(
                    estudiante.getId(), TipoNotificacion.ALERTA_BIENESTAR, inicioDia, finDia);
            if (yaNotificadoHoy) continue;

            List<RegistroEmocional> ultimosTres = registroEmocionalRepository
                    .findTop30ByEstudianteCorreoOrderByFechaDesc(estudiante.getCorreo())
                    .stream().limit(3).toList();

            long diasBajos = ultimosTres.stream()
                    .filter(r -> r.getEstadoAnimo() == EstadoAnimo.MUY_MAL || r.getEstadoAnimo() == EstadoAnimo.MAL)
                    .count();

            if (diasBajos >= 3) {
                notificacionService.crear(estudiante, TipoNotificacion.ALERTA_BIENESTAR,
                        "Alerta de bienestar emocional",
                        "Hemos notado un estado de ánimo bajo en tus últimos registros. Te recomendamos agendar una cita con un psicólogo de MindU.",
                        null);
            } else if (diasBajos >= 1) {
                notificacionService.crear(estudiante, TipoNotificacion.ALERTA_BIENESTAR,
                        "Recomendación de bienestar",
                        "Notamos algunos días difíciles recientemente. Prueba una pausa de respiración o una caminata corta.",
                        null);
            }
        }
    }

    // HU021 — corre todos los días 8:00 p.m.
    @Scheduled(cron = "0 0 20 * * *")
    public void enviarRecordatoriosDeSeguimiento() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        for (Estudiante estudiante : estudianteRepository.findByActivoTrue()) {
            boolean yaRegistroHoy = registroEmocionalRepository
                    .findByEstudianteIdAndFecha(estudiante.getId(), hoy).isPresent();
            if (yaRegistroHoy) continue;

            boolean yaNotificadoHoy = notificacionRepository.existsByUsuarioIdAndTipoAndCreadoEnBetween(
                    estudiante.getId(), TipoNotificacion.SEGUIMIENTO_EMOCIONAL, inicioDia, finDia);
            if (yaNotificadoHoy) continue;

            notificacionService.crear(estudiante, TipoNotificacion.SEGUIMIENTO_EMOCIONAL,
                    "Registra tu estado de ánimo",
                    "Aún no has registrado cómo te sientes hoy. Tómate un minuto para continuar tu seguimiento emocional.",
                    null);
        }
    }
}