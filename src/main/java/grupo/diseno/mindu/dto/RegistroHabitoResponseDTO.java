package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.CalidadSueno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroHabitoResponseDTO {

    private Long id;
    private LocalDate fecha;
    private Double horasSueno;
    private CalidadSueno calidadSueno;
    private Boolean desayuno;
    private Boolean almuerzo;
    private Boolean cena;
    private Integer vasosAgua;
    private Integer pasos;
    private Integer metaPasos;
}