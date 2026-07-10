package grupo.diseno.mindu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaHabitoDTO {

    private LocalDate fecha;
    private String diaAbreviado; // L, M, X, J, V, S, D
    private boolean registrado;
}