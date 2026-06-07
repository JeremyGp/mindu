package grupo.diseno.mindu.dto;

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
public class RecomendacionCitaDTO {

    private Long psicologoId;
    private String psicologoNombreCompleto;
    private String psicologoEspecialidad;
    private String modalidad;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Integer prioridad;
    private Boolean generadaPorIA;
    private String modeloIA;
}
