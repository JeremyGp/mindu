package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.NotificacionResponseDTO;
import grupo.diseno.mindu.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<?> listar(Principal principal) {
        return ResponseEntity.ok(notificacionService.listarMisNotificaciones(principal.getName()));
    }

    @GetMapping("/no-leidas/conteo")
    public ResponseEntity<?> contarNoLeidas(Principal principal) {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(principal.getName())));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id, Principal principal) {
        try {
            return ResponseEntity.ok(notificacionService.marcarComoLeida(id, principal.getName()));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/leer-todas")
    public ResponseEntity<?> marcarTodasComoLeidas(Principal principal) {
        notificacionService.marcarTodasComoLeidas(principal.getName());
        return ResponseEntity.ok(Map.of("mensaje", "Notificaciones marcadas como leídas"));
    }
}