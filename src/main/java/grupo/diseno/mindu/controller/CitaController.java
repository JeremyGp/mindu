package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.ActualizarEstadoRequest;
import grupo.diseno.mindu.dto.AgendarCitaRequest;
import grupo.diseno.mindu.dto.CitaResponseDTO;
import grupo.diseno.mindu.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    public ResponseEntity<?> agendar(@Valid @RequestBody AgendarCitaRequest request, Principal principal) {
        try {
            CitaResponseDTO response = citaService.agendarCita(request, principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mis-citas")
    public ResponseEntity<?> listarMisCitas(Principal principal) {
        try {
            List<CitaResponseDTO> response = citaService.listarMisCitas(principal.getName());
            if (response.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "mensaje", "No tienes citas registradas actualmente.",
                        "citas", response
                ));
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Long id, Principal principal) {
        try {
            CitaResponseDTO response = citaService.obtenerCitaDetalle(id, principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoRequest request,
            Principal principal) {
        try {
            CitaResponseDTO response = citaService.actualizarEstado(id, request.getEstado(), principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
