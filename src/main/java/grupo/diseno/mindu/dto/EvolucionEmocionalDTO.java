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
public class EvolucionEmocionalDTO {

    private List<RegistroEmocionalResponseDTO> registros;
    private double promedio;
    private String tendencia; // "MEJORANDO", "EMPEORANDO", "ESTABLE"
}