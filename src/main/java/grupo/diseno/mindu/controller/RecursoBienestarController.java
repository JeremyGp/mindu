package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.RecursoBienestarDTO;
import grupo.diseno.mindu.model.TipoRecurso;
import grupo.diseno.mindu.service.RecursoBienestarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bienestar")
@RequiredArgsConstructor
public class RecursoBienestarController {

    private final RecursoBienestarService recursoBienestarService;

    // HU010 y HU011: listar ejercicios de respiración o meditaciones guiadas
    @GetMapping("/recursos")
    public ResponseEntity<?> listarPorTipo(@RequestParam TipoRecurso tipo) {
        return ResponseEntity.ok(recursoBienestarService.listarPorTipo(tipo));
    }

    // HU012: detalle de un recurso puntual (también usado para técnicas de crisis)
    @GetMapping("/recursos/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recursoBienestarService.obtenerDetalle(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}