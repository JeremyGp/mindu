package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.DiaHabitoDTO;
import grupo.diseno.mindu.dto.RegistroHabitoRequest;
import grupo.diseno.mindu.dto.RegistroHabitoResponseDTO;
import grupo.diseno.mindu.service.HabitoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habitos")
@RequiredArgsConstructor
public class HabitoController {

    private final HabitoService habitoService;

    // Precarga los widgets de Sueño, Hidratación, Comidas y Actividad con lo ya registrado hoy
    @GetMapping("/hoy")
    public ResponseEntity<?> obtenerHoy(Principal principal) {
        try {
            RegistroHabitoResponseDTO response = habitoService.obtenerHabitoDeHoy(principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Cada widget llama a este mismo endpoint enviando solo el campo que cambió
    @PutMapping("/hoy")
    public ResponseEntity<?> actualizarHoy(@Valid @RequestBody RegistroHabitoRequest request, Principal principal) {
        try {
            RegistroHabitoResponseDTO response = habitoService.actualizarHabitoHoy(request, principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Para el "Progreso Semanal" (círculos L M X J V S D)
    @GetMapping("/semana")
    public ResponseEntity<?> obtenerSemana(Principal principal) {
        try {
            List<DiaHabitoDTO> response = habitoService.obtenerResumenSemanal(principal.getName());
            return ResponseEntity.ok(Map.of("dias", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}