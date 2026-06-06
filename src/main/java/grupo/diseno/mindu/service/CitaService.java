package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.AgendarCitaRequest;
import grupo.diseno.mindu.dto.CitaResponseDTO;
import grupo.diseno.mindu.model.*;
import grupo.diseno.mindu.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final EstudianteRepository estudianteRepository;
    private final PsicologoRepository psicologoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DisponibilidadRepository disponibilidadRepository;

    @Transactional
    public CitaResponseDTO agendarCita(AgendarCitaRequest request, String estudianteCorreo) {
        Estudiante estudiante = estudianteRepository.findByCorreo(estudianteCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        if (!estudiante.getActivo()) {
            throw new IllegalArgumentException("La cuenta del estudiante está inactiva");
        }

        Psicologo psicologo = psicologoRepository.findById(request.getPsicologoId())
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo no encontrado"));

        if (!psicologo.getActivo()) {
            throw new IllegalArgumentException("El psicólogo seleccionado no está activo");
        }

        // Validar que el psicólogo tenga ese horario configurado como disponible
        Disponibilidad disponibilidad = disponibilidadRepository.findByPsicologoIdAndFechaAndHora(
                psicologo.getId(), request.getFecha(), request.getHora())
                .orElseThrow(() -> new IllegalArgumentException("El psicólogo no tiene disponibilidad configurada para esta fecha y hora"));

        if (!disponibilidad.getDisponible()) {
            throw new IllegalArgumentException("El horario seleccionado ya no está disponible");
        }

        // Validar cruce de horario para el psicólogo (como medida adicional de seguridad)
        boolean psicologoOcupado = citaRepository.existsOverlappingPsicologo(
                psicologo.getId(),
                request.getFecha(),
                request.getHora(),
                EstadoCita.CANCELADA
        );
        if (psicologoOcupado) {
            throw new IllegalArgumentException("El psicólogo ya tiene una cita programada para esta fecha y hora");
        }

        // Validar cruce de horario para el estudiante
        boolean estudianteOcupado = citaRepository.existsOverlappingEstudiante(
                estudiante.getId(),
                request.getFecha(),
                request.getHora(),
                EstadoCita.CANCELADA
        );
        if (estudianteOcupado) {
            throw new IllegalArgumentException("Ya tienes otra cita programada para esta misma fecha y hora");
        }

        // Reservar el horario
        disponibilidad.setDisponible(false);
        disponibilidadRepository.save(disponibilidad);

        Cita cita = Cita.builder()
                .fecha(request.getFecha())
                .hora(request.getHora())
                .modalidad(request.getModalidad())
                .estudiante(estudiante)
                .psicologo(psicologo)
                .estado(EstadoCita.PENDIENTE)
                .build();

        Cita savedCita = citaRepository.save(cita);
        return mapToDTO(savedCita);
    }

    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarMisCitas(String usuarioCorreo) {
        Usuario usuario = usuarioRepository.findByCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Cita> citas;
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            citas = citaRepository.findByEstudianteCorreoOrderByFechaAscHoraAsc(usuarioCorreo);
        } else if (usuario.getRol() == Rol.PSICOLOGO) {
            citas = citaRepository.findByPsicologoCorreoOrderByFechaAscHoraAsc(usuarioCorreo);
        } else {
            // ADMIN o similar: listamos todas las citas
            citas = citaRepository.findAll();
        }

        return citas.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public CitaResponseDTO actualizarEstado(Long citaId, EstadoCita nuevoEstado, String usuarioCorreo) {
        Usuario usuario = usuarioRepository.findByCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        Rol userRol = usuario.getRol();

        // Validaciones de permisos por rol
        if (userRol == Rol.ESTUDIANTE) {
            // El estudiante solo puede cancelar su propia cita
            if (!cita.getEstudiante().getCorreo().equals(usuarioCorreo)) {
                throw new SecurityException("No tienes permiso para modificar esta cita");
            }
            if (nuevoEstado != EstadoCita.CANCELADA) {
                throw new IllegalArgumentException("Un estudiante solo puede cancelar sus citas");
            }
        } else if (userRol == Rol.PSICOLOGO) {
            // El psicólogo solo puede modificar el estado de sus propias citas
            if (!cita.getPsicologo().getCorreo().equals(usuarioCorreo)) {
                throw new SecurityException("No tienes permiso para modificar esta cita");
            }
            // El psicólogo puede cambiar a cualquier estado (CONFIRMADA, CANCELADA, COMPLETADA)
        } else if (userRol != Rol.ADMIN) {
            throw new SecurityException("Rol no autorizado para modificar citas");
        }

        // Si ya está completada o cancelada, no permitir cambios de estado
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("No se puede cambiar el estado de una cita que ya ha sido completada o cancelada");
        }

        // Si se cancela, liberar la disponibilidad
        if (nuevoEstado == EstadoCita.CANCELADA) {
            disponibilidadRepository.findByPsicologoIdAndFechaAndHora(
                    cita.getPsicologo().getId(), cita.getFecha(), cita.getHora())
                    .ifPresent(d -> {
                        d.setDisponible(true);
                        disponibilidadRepository.save(d);
                    });
        }

        cita.setEstado(nuevoEstado);
        Cita updatedCita = citaRepository.save(cita);
        return mapToDTO(updatedCita);
    }

    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaDetalle(Long id, String usuarioCorreo) {
        Usuario usuario = usuarioRepository.findByCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        Rol userRol = usuario.getRol();

        // Validaciones de permisos
        if (userRol == Rol.ESTUDIANTE) {
            if (!cita.getEstudiante().getCorreo().equals(usuarioCorreo)) {
                throw new SecurityException("No tienes permiso para ver los detalles de esta cita");
            }
        } else if (userRol == Rol.PSICOLOGO) {
            if (!cita.getPsicologo().getCorreo().equals(usuarioCorreo)) {
                throw new SecurityException("No tienes permiso para ver los detalles de esta cita");
            }
        } else if (userRol != Rol.ADMIN) {
            throw new SecurityException("Rol no autorizado para visualizar citas");
        }

        return mapToDTO(cita);
    }

    private CitaResponseDTO mapToDTO(Cita cita) {
        return CitaResponseDTO.builder()
                .id(cita.getId())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .modalidad(cita.getModalidad())
                .estado(cita.getEstado())
                .estudianteId(cita.getEstudiante().getId())
                .estudianteNombreCompleto(cita.getEstudiante().getNombre() + " " + cita.getEstudiante().getApellido())
                .psicologoId(cita.getPsicologo().getId())
                .psicologoNombreCompleto(cita.getPsicologo().getNombre() + " " + cita.getPsicologo().getApellido())
                .psicologoEspecialidad(cita.getPsicologo().getEspecialidad())
                .build();
    }
}
