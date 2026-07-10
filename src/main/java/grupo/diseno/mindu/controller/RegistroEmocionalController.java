package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.EvolucionEmocionalDTO;
import grupo.diseno.mindu.dto.RecomendacionBienestarDTO;
import grupo.diseno.mindu.dto.RegistroEmocionalRequest;
import grupo.diseno.mindu.dto.RegistroEmocionalResponseDTO;
import grupo.diseno.mindu.service.RegistroEmocionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emociones")
@RequiredArgsConstructor
public class RegistroEmocionalController {

    private final RegistroEmocionalService registroEmocionalService;

    // HU007: registrar el estado emocional diario
    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroEmocionalRequest request, Principal principal) {
        try {
            RegistroEmocionalResponseDTO response = registroEmocionalService.registrarEstado(request, principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Precarga el formulario si el estudiante ya registró su estado hoy
    @GetMapping("/hoy")
    public ResponseEntity<?> obtenerHoy(Principal principal) {
        try {
            RegistroEmocionalResponseDTO response = registroEmocionalService.obtenerRegistroDeHoy(principal.getName());
            return ResponseEntity.ok(Map.of("registro", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // HU008: visualizar la evolución emocional
    @GetMapping("/evolucion")
    public ResponseEntity<?> obtenerEvolucion(
            @RequestParam(defaultValue = "30") int dias,
            Principal principal) {
        try {
            EvolucionEmocionalDTO response = registroEmocionalService.obtenerEvolucion(principal.getName(), dias);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // HU009: recomendaciones personalizadas según el estado emocional
    @GetMapping("/recomendaciones")
    public ResponseEntity<?> obtenerRecomendaciones(Principal principal) {
        try {
            List<RecomendacionBienestarDTO> response = registroEmocionalService.obtenerRecomendaciones(principal.getName());
            return ResponseEntity.ok(Map.of("recomendaciones", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}