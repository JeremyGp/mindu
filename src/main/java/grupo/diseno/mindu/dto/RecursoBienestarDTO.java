package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.TipoRecurso;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoBienestarDTO {
    private Long id;
    private TipoRecurso tipo;
    private String titulo;
    private String descripcion;
    private Integer duracionSegundos;
    private List<String> pasos;
}