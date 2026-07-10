package grupo.diseno.mindu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacionBienestarDTO {

    private String categoria;
    private String mensaje;
    private String nivel; // "INFO" o "ALERTA"
}