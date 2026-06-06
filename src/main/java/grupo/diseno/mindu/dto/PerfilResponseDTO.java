package grupo.diseno.mindu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;

    // Solo Estudiantes
    private String dni;
    private String codigo;
    private Integer edad;
    private Integer cicloAcademico;

    // Solo Psicólogos
    private String especialidad;
    private String modalidad;

}
