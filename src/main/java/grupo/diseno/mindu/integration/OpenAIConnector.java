package grupo.diseno.mindu.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OpenAIConnector {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String transcriptionModel;
    private final String responsesUrl;
    private final String transcriptionsUrl;

    public OpenAIConnector(
            ObjectMapper objectMapper,
            @Value("${openai.api.key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${openai.transcription.model:gpt-4o-mini-transcribe}") String transcriptionModel,
            @Value("${openai.responses.url:https://api.openai.com/v1/responses}") String responsesUrl,
            @Value("${openai.transcriptions.url:https://api.openai.com/v1/audio/transcriptions}") String transcriptionsUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.transcriptionModel = transcriptionModel;
        this.responsesUrl = responsesUrl;
        this.transcriptionsUrl = transcriptionsUrl;
    }

    public boolean estaConfigurado() {
        return !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }

    public String generarJson(String instrucciones, String entradaUsuario) {
        String respuesta = generarRespuesta(instrucciones, entradaUsuario, Map.of(
                "format", Map.of("type", "json_object")
        ));
        return respuesta;
    }

    public String generarTexto(String instrucciones, String entradaUsuario) {
        return generarRespuesta(instrucciones, entradaUsuario, null);
    }

    private String generarRespuesta(String instrucciones, String entradaUsuario, Map<String, Object> textOptions) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("No se configuro OPENAI_API_KEY");
        }

        try {
            Map<String, Object> payload;
            if (textOptions == null) {
                payload = Map.of(
                        "model", model,
                        "instructions", instrucciones,
                        "input", entradaUsuario
                );
            } else {
                payload = Map.of(
                        "model", model,
                        "input", List.of(
                                Map.of("role", "system", "content", instrucciones),
                                Map.of("role", "user", "content", entradaUsuario)
                        ),
                        "text", textOptions
                );
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(responsesUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI respondio con estado " + response.statusCode() + ": " + response.body());
            }

            return extraerTexto(objectMapper.readTree(response.body()));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo procesar la respuesta de OpenAI", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La solicitud a OpenAI fue interrumpida", e);
        }
    }

    public String transcribirAudio(byte[] audio, String filename, String contentType) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("No se configuro OPENAI_API_KEY");
        }
        if (audio == null || audio.length == 0) {
            throw new IllegalArgumentException("El archivo de audio esta vacio");
        }

        try {
            String boundary = "----MindUBoundary" + UUID.randomUUID();
            BodyPublisher body = construirMultipart(boundary, audio, filename, contentType);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(transcriptionsUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(body)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI respondio con estado " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode text = root.get("text");
            if (text == null || !text.isTextual()) {
                throw new IllegalStateException("No se encontro texto en la transcripcion");
            }
            return text.asText();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo transcribir el audio", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La transcripcion fue interrumpida", e);
        }
    }

    private BodyPublisher construirMultipart(String boundary, byte[] audio, String filename, String contentType) {
        String safeFilename = (filename == null || filename.isBlank()) ? "audio.webm" : filename;
        String safeContentType = (contentType == null || contentType.isBlank()) ? "audio/webm" : contentType;
        List<byte[]> partes = new ArrayList<>();

        agregarCampo(partes, boundary, "model", transcriptionModel);
        agregarCampo(partes, boundary, "language", "es");
        agregarArchivo(partes, boundary, "file", safeFilename, safeContentType, audio);
        partes.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(partes);
    }

    private void agregarCampo(List<byte[]> partes, String boundary, String nombre, String valor) {
        String campo = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + nombre + "\"\r\n\r\n"
                + valor + "\r\n";
        partes.add(campo.getBytes(StandardCharsets.UTF_8));
    }

    private void agregarArchivo(List<byte[]> partes, String boundary, String nombre, String filename, String contentType, byte[] contenido) {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + nombre + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n";
        partes.add(header.getBytes(StandardCharsets.UTF_8));
        partes.add(contenido);
        partes.add("\r\n".getBytes(StandardCharsets.UTF_8));
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

        throw new IllegalStateException("No se encontro texto en la respuesta de OpenAI");
    }
}
