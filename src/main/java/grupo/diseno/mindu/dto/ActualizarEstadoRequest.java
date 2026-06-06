package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.EstadoCita;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoCita estado;

}
