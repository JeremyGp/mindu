package grupo.diseno.mindu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private String password; // Contraseña nueva (opcional)

    // Campos específicos de Estudiante (opcionales en el DTO, validados en el service)
    private Integer edad;
    private Integer cicloAcademico;

    // Campos específicos de Psicólogo (opcionales en el DTO, validados en el service)
    private String especialidad;
    private String modalidad;

}
