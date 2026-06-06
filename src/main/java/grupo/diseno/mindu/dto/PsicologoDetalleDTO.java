package grupo.diseno.mindu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsicologoDetalleDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String especialidad;
    private String modalidad;
    private String correo;
    private List<DisponibilidadDTO> horariosDisponibles;

}
