package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.EstadoAnimo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroEmocionalRequest {

    @NotNull(message = "El estado de ánimo es obligatorio")
    private EstadoAnimo estadoAnimo;

    private LocalDate fecha;

    @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
    private String notas;
}