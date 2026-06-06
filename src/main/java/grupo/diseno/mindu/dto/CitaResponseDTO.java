package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaResponseDTO {

    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String modalidad;
    private EstadoCita estado;

    private Long estudianteId;
    private String estudianteNombreCompleto;

    private Long psicologoId;
    private String psicologoNombreCompleto;
    private String psicologoEspecialidad;

}
