package grupo.diseno.mindu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadDTO {

    private Long id;
    private LocalDate fecha;
    private LocalTime hora;

}
