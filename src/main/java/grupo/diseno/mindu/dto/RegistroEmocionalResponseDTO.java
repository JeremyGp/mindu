package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.EstadoAnimo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEmocionalResponseDTO {

    private Long id;
    private LocalDate fecha;
    private EstadoAnimo estadoAnimo;
    private String estadoAnimoEtiqueta;
    private int valorNumerico;
    private String notas;
    private LocalDateTime creadoEn;
}