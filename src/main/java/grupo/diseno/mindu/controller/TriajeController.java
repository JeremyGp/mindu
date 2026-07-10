package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.TranscripcionResponse;
import grupo.diseno.mindu.dto.TriajeChatRequest;
import grupo.diseno.mindu.dto.TriajeChatResponse;
import grupo.diseno.mindu.integration.OpenAIConnector;
import grupo.diseno.mindu.integration.TriajeAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/triaje")
@RequiredArgsConstructor
public class TriajeController {

    private final TriajeAIService triajeAIService;
    private final OpenAIConnector openAIConnector;

    @PostMapping("/chat")
    public ResponseEntity<TriajeChatResponse> chat(@Valid @RequestBody TriajeChatRequest request) {
        return ResponseEntity.ok(triajeAIService.responder(request));
    }

    @PostMapping(value = "/transcribir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscripcionResponse> transcribir(@RequestParam("audio") MultipartFile audio) throws IOException {
        String texto = openAIConnector.transcribirAudio(
                audio.getBytes(),
                audio.getOriginalFilename(),
                audio.getContentType()
        );
        return ResponseEntity.ok(new TranscripcionResponse(texto));
    }
}
