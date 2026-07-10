package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.CalidadSueno;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Todos los campos son opcionales: cada widget del frontend envía solo lo que cambió (actualización parcial)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHabitoRequest {

    @DecimalMin(value = "0.0", message = "Las horas dormidas no pueden ser negativas")
    @DecimalMax(value = "24.0", message = "Las horas dormidas no pueden superar 24")
    private Double horasSueno;

    private CalidadSueno calidadSueno;

    private Boolean desayuno;
    private Boolean almuerzo;
    private Boolean cena;

    @Min(value = 0, message = "Los vasos de agua no pueden ser negativos")
    @Max(value = 20, message = "Cantidad de vasos de agua no válida")
    private Integer vasosAgua;

    @Min(value = 0, message = "Los pasos no pueden ser negativos")
    private Integer pasos;

    @Min(value = 1000, message = "La meta de pasos debe ser un valor razonable")
    private Integer metaPasos;
}