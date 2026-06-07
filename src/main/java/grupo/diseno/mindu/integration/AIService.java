package grupo.diseno.mindu.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grupo.diseno.mindu.dto.RecomendacionCitaDTO;
import grupo.diseno.mindu.model.Estudiante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIService {

    private final OpenAIConnector openAIConnector;
    private final ObjectMapper objectMapper;

    public List<RecomendacionCitaDTO> recomendarCitas(Estudiante estudiante, List<RecomendacionCitaDTO> candidatos) {
        List<RecomendacionCitaDTO> base = candidatos.stream()
                .sorted(Comparator.comparing(RecomendacionCitaDTO::getFecha).thenComparing(RecomendacionCitaDTO::getHora))
                .limit(8)
                .toList();

        if (base.isEmpty()) {
            return List.of();
        }

        if (!openAIConnector.estaConfigurado()) {
            return fallback(base, "Modelo IA no configurado. Configure OPENAI_API_KEY para activar recomendaciones generadas por IA.");
        }

        try {
            String respuesta = openAIConnector.generarJson(instrucciones(), construirEntrada(estudiante, base));
            return mapearRespuestaIA(base, respuesta);
        } catch (RuntimeException e) {
            System.err.println("ERROR OpenAI: " + e.getMessage());
            return fallback(base, "Se usó recomendación con disponibilidad validada.");
        }
    }

    private String instrucciones() {
        return """
                Eres el asistente de agendamiento psicológico de MindU.
                Debes elegir las 3 mejores citas para un estudiante universitario.
                Usa únicamente los candidatos proporcionados; no inventes psicólogos, fechas ni horas.
                Prioriza menor tiempo de espera, especialidad psicológica pertinente, modalidad del profesional y variedad de opciones.
                Responde únicamente JSON válido con esta forma:
                {
                  "recommendations": [
                    {"candidateId": 1, "reason": "motivo breve en español"},
                    {"candidateId": 2, "reason": "motivo breve en español"}
                  ]
                }
                """;
    }

    private String construirEntrada(Estudiante estudiante, List<RecomendacionCitaDTO> candidatos) throws RuntimeException {
        try {
            List<Map<String, Object>> candidatosJson = new ArrayList<>();
            for (int i = 0; i < candidatos.size(); i++) {
                RecomendacionCitaDTO c = candidatos.get(i);
                candidatosJson.add(Map.of(
                        "candidateId", i + 1,
                        "psicologoId", c.getPsicologoId(),
                        "psicologoNombreCompleto", c.getPsicologoNombreCompleto(),
                        "especialidad", c.getPsicologoEspecialidad(),
                        "modalidad", c.getModalidad(),
                        "fecha", c.getFecha().toString(),
                        "hora", c.getHora().toString()
                ));
            }

            Map<String, Object> contexto = new LinkedHashMap<>();
            contexto.put("estudiante", Map.of(
                    "edad", estudiante.getEdad(),
                    "cicloAcademico", estudiante.getCicloAcademico()
            ));
            contexto.put("candidatosDisponibles", candidatosJson);

            return objectMapper.writeValueAsString(contexto);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo construir el contexto para la IA", e);
        }
    }

    private List<RecomendacionCitaDTO> mapearRespuestaIA(List<RecomendacionCitaDTO> base, String respuestaJson) {
        try {
            JsonNode root = objectMapper.readTree(respuestaJson);
            JsonNode recommendations = root.get("recommendations");
            if (recommendations == null || !recommendations.isArray()) {
                return fallback(base, "El modelo IA no devolvió recomendaciones con el formato esperado.");
            }

            List<RecomendacionCitaDTO> resultado = new ArrayList<>();
            int prioridad = 1;
            for (JsonNode item : recommendations) {
                int candidateId = item.path("candidateId").asInt(0);
                if (candidateId < 1 || candidateId > base.size()) {
                    continue;
                }

                RecomendacionCitaDTO recomendado = copiar(base.get(candidateId - 1));
                recomendado.setPrioridad(prioridad++);
                recomendado.setMotivo(Optional.ofNullable(item.get("reason"))
                        .map(JsonNode::asText)
                        .filter(reason -> !reason.isBlank())
                        .orElse("Recomendado por el modelo IA con disponibilidad validada."));
                recomendado.setGeneradaPorIA(true);
                recomendado.setModeloIA(openAIConnector.getModel());
                resultado.add(recomendado);

                if (resultado.size() == 3) {
                    break;
                }
            }

            if (resultado.isEmpty()) {
                return fallback(base, "El modelo IA no seleccionó candidatos válidos.");
            }

            return resultado;
        } catch (Exception e) {
            return fallback(base, "No se pudo interpretar la respuesta del modelo IA.");
        }
    }

    private List<RecomendacionCitaDTO> fallback(List<RecomendacionCitaDTO> base, String motivo) {
        List<RecomendacionCitaDTO> resultado = new ArrayList<>();
        for (int i = 0; i < Math.min(3, base.size()); i++) {
            RecomendacionCitaDTO recomendacion = copiar(base.get(i));
            recomendacion.setPrioridad(i + 1);
            recomendacion.setMotivo(motivo);
            recomendacion.setGeneradaPorIA(false);
            recomendacion.setModeloIA(openAIConnector.getModel());
            resultado.add(recomendacion);
        }
        return resultado;
    }

    private RecomendacionCitaDTO copiar(RecomendacionCitaDTO original) {
        return RecomendacionCitaDTO.builder()
                .psicologoId(original.getPsicologoId())
                .psicologoNombreCompleto(original.getPsicologoNombreCompleto())
                .psicologoEspecialidad(original.getPsicologoEspecialidad())
                .modalidad(original.getModalidad())
                .fecha(original.getFecha())
                .hora(original.getHora())
                .motivo(original.getMotivo())
                .prioridad(original.getPrioridad())
                .generadaPorIA(original.getGeneradaPorIA())
                .modeloIA(original.getModeloIA())
                .build();
    }
}
