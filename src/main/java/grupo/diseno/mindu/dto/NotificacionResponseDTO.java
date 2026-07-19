package grupo.diseno.mindu.dto;

import grupo.diseno.mindu.model.TipoNotificacion;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponseDTO {
    private Long id;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private Long referenciaId;
    private LocalDateTime creadoEn;
}