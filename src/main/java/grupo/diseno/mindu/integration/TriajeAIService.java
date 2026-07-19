package grupo.diseno.mindu.integration;

import grupo.diseno.mindu.dto.TriajeChatRequest;
import grupo.diseno.mindu.dto.TriajeChatResponse;
import grupo.diseno.mindu.dto.TriajeMensajeDTO;
import grupo.diseno.mindu.model.TipoNotificacion;
import grupo.diseno.mindu.repository.EstudianteRepository;
import grupo.diseno.mindu.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TriajeAIService {

    private final OpenAIConnector openAIConnector;
    private final EstudianteRepository estudianteRepository;
    private final NotificacionService notificacionService;

    private static final List<String> PALABRAS_RIESGO = List.of(
            "suicid", "matarme", "quitarme la vida", "no quiero vivir", "no vale la pena vivir",
            "hacerme daño", "autolesion", "autolesión", "cortarme", "quiero morir", "acabar con todo"
    );

    public TriajeChatResponse responder(TriajeChatRequest request, String correoUsuario) {
        boolean hayRiesgo = detectarRiesgo(request.mensaje());

        if (hayRiesgo) {
            notificarRiesgo(correoUsuario);
        }

        if (!openAIConnector.estaConfigurado()) {
            return new TriajeChatResponse(
                    "Todavia no tengo configurada la clave de IA en el servidor. Configura OPENAI_API_KEY y vuelve a intentar.",
                    false,
                    openAIConnector.getModel(),
                    hayRiesgo
            );
        }

        try {
            String respuesta = openAIConnector.generarTexto(instrucciones(), construirConversacion(request));
            return new TriajeChatResponse(respuesta, true, openAIConnector.getModel(), hayRiesgo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new TriajeChatResponse(
                    "ERROR: " + e.getMessage(),
                    false,
                    openAIConnector.getModel(),
                    hayRiesgo
            );
        }
    }

    private boolean detectarRiesgo(String mensaje) {
        if (mensaje == null) return false;
        String texto = mensaje.toLowerCase(Locale.ROOT);
        return PALABRAS_RIESGO.stream().anyMatch(texto::contains);
    }

    private void notificarRiesgo(String correoUsuario) {
        estudianteRepository.findByCorreo(correoUsuario).ifPresent(estudiante ->
                notificacionService.crear(estudiante, TipoNotificacion.ALERTA_BIENESTAR,
                        "Detectamos que podrías necesitar apoyo",
                        "En tu conversación de triaje notamos señales de alerta. Te recomendamos agendar una cita con un psicólogo cuanto antes.",
                        null)
        );
    }

    private String instrucciones() {
        return """
            Eres el asistente de triaje psicológico de MindU para estudiantes universitarios.

            Tu objetivo es brindar una primera orientación emocional, escuchar al estudiante y acompañarlo de manera cercana.

            Estilo de conversación:
            - Habla como una persona amable y cercana, no como un robot ni como un asesor formal.
            - Sé empático, pero evita exagerar frases de ánimo.
            - Usa un tono cálido, tranquilo y profesional.
            - Responde de manera clara y natural.

            Forma de responder:
            - Adapta la longitud de la respuesta a la situación.
            - Para problemas simples, responde brevemente con 2 o 3 recomendaciones principales.
            - Usa listas solamente cuando ayuden a ordenar la información.
            - No repitas constantemente frases como "estoy aquí para ayudarte" o "gracias por compartir".
            - No hagas siempre una pregunta al final.

            Sobre las preguntas:
            - Si necesitas conocer más información para orientar mejor, realiza una sola pregunta breve.
            - Si el estudiante ya explicó su situación o solo pidió recomendaciones generales, responde y cierra la conversación naturalmente.
            - Si el estudiante indica que ya recibió ayuda o no tiene más dudas, despídete de forma breve sin hacer nuevas preguntas.

            Límites:
            - No diagnostiques trastornos psicológicos.
            - No reemplaces a un psicólogo profesional.
            - No inventes citas ni disponibilidad de profesionales.
            - Si existe riesgo de suicidio, autolesión, violencia o emergencia, recomienda buscar ayuda profesional urgente y contactar servicios de emergencia o personas de confianza.

            Siempre responde en español claro.
                """;
    }

    private String construirConversacion(TriajeChatRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Historial reciente del chat:\n");

        List<TriajeMensajeDTO> historial = request.historial() == null ? List.of() : request.historial();
        historial.stream()
                .skip(Math.max(0, historial.size() - 10))
                .forEach(mensaje -> builder
                        .append(normalizarRol(mensaje.rol()))
                        .append(": ")
                        .append(mensaje.contenido())
                        .append('\n'));

        builder.append("\nMensaje actual del estudiante:\n");
        builder.append(request.mensaje());
        return builder.toString();
    }

    private String normalizarRol(String rol) {
        if ("assistant".equalsIgnoreCase(rol) || "ia".equalsIgnoreCase(rol)) {
            return "Asistente";
        }
        return "Estudiante";
    }
}