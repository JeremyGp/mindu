package grupo.diseno.mindu.dto;

public record TriajeChatResponse(
        String respuesta,
        boolean generadaPorIA,
        String modeloIA
) {
}
