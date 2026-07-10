package grupo.diseno.mindu.dto;

import jakarta.validation.constraints.NotBlank;

public record TriajeMensajeDTO(
        @NotBlank String rol,
        @NotBlank String contenido
) {
}
