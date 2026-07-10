package grupo.diseno.mindu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TriajeChatRequest(
        @NotBlank String mensaje,
        List<@Valid TriajeMensajeDTO> historial
) {
}
