package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.DisponibilidadDTO;
import grupo.diseno.mindu.dto.PsicologoDTO;
import grupo.diseno.mindu.dto.PsicologoDetalleDTO;
import grupo.diseno.mindu.model.Psicologo;
import grupo.diseno.mindu.repository.DisponibilidadRepository;
import grupo.diseno.mindu.repository.PsicologoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PsicologoService {

    private final PsicologoRepository psicologoRepository;
    private final DisponibilidadRepository disponibilidadRepository;

    @Transactional(readOnly = true)
    public List<PsicologoDTO> listarDisponibles() {
        return psicologoRepository.findByActivoTrue().stream()
                .map(p -> new PsicologoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getEspecialidad(),
                        p.getModalidad()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PsicologoDTO> listarPorModalidad(String modalidad) {
        return psicologoRepository.findByActivoTrueAndModalidad(modalidad).stream()
                .map(p -> new PsicologoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getEspecialidad(),
                        p.getModalidad()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PsicologoDetalleDTO obtenerDetalle(Long id) {
        Psicologo psicologo = psicologoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo no encontrado"));

        if (!psicologo.getActivo()) {
            throw new IllegalArgumentException("El psicólogo seleccionado no está activo");
        }

        List<DisponibilidadDTO> horarios = disponibilidadRepository
                .findByPsicologoIdAndDisponibleTrueAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(id, LocalDate.now())
                .stream()
                .map(d -> new DisponibilidadDTO(d.getId(), d.getFecha(), d.getHora()))
                .toList();

        return PsicologoDetalleDTO.builder()
                .id(psicologo.getId())
                .nombre(psicologo.getNombre())
                .apellido(psicologo.getApellido())
                .especialidad(psicologo.getEspecialidad())
                .modalidad(psicologo.getModalidad())
                .correo(psicologo.getCorreo())
                .horariosDisponibles(horarios)
                .build();
    }
}