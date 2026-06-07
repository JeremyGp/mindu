package grupo.diseno.mindu.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class OpenAIConnector {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String responsesUrl;

    public OpenAIConnector(
            ObjectMapper objectMapper,
            @Value("${openai.api.key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${openai.responses.url:https://api.openai.com/v1/responses}") String responsesUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.responsesUrl = responsesUrl;
    }

    public boolean estaConfigurado() {
        return !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }

    public String generarJson(String instrucciones, String entradaUsuario) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("No se configuró OPENAI_API_KEY");
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "input", java.util.List.of(
                            Map.of(
                                    "role", "system",
                                    "content", instrucciones
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", entradaUsuario
                            )
                    ),
                    "text", Map.of(
                            "format", Map.of(
                                    "type", "json_object"
                            )
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(responsesUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI respondió con estado " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return extraerTexto(root);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo procesar la respuesta de OpenAI", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La solicitud a OpenAI fue interrumpida", e);
        }
    }

    private String extraerTexto(JsonNode root) {
        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            return outputText.asText();
        }

        JsonNode output = root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode text = contentItem.get("text");
                        if (text != null && text.isTextual()) {
                            return text.asText();
                        }
                    }
                }
            }
        }

        throw new IllegalStateException("No se encontró texto en la respuesta de OpenAI");
    }
}
