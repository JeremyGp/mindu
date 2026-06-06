package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.ActualizarPerfilRequest;
import grupo.diseno.mindu.dto.PerfilResponseDTO;
import grupo.diseno.mindu.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    // GET /api/usuarios/perfil -> obtener perfil del usuario autenticado
    @GetMapping
    public ResponseEntity<?> obtenerPerfil(Principal principal) {
        try {
            PerfilResponseDTO perfil = usuarioService.obtenerPerfil(principal.getName());
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/usuarios/perfil -> actualizar perfil de manera segura
    @PutMapping
    public ResponseEntity<?> actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request, Principal principal) {
        try {
            PerfilResponseDTO perfil = usuarioService.actualizarPerfil(principal.getName(), request);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
