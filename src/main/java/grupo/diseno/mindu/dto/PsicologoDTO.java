package grupo.diseno.mindu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PsicologoDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String especialidad;
    private String modalidad;
}