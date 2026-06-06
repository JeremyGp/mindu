package grupo.diseno.mindu.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo dígitos")
    private String dni;

    @NotBlank(message = "El código universitario es obligatorio")
    private String codigo;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 16, message = "La edad mínima es 16 años")
    private Integer edad;

    @NotNull(message = "El ciclo académico es obligatorio")
    @Min(value = 1, message = "El ciclo mínimo es 1")
    @Max(value = 12, message = "El ciclo máximo es 12")
    private Integer cicloAcademico;
}
