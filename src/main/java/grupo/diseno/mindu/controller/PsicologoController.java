package grupo.diseno.mindu.controller;

import grupo.diseno.mindu.dto.PsicologoDTO;
import grupo.diseno.mindu.dto.PsicologoDetalleDTO;
import grupo.diseno.mindu.service.PsicologoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psicologos")
@RequiredArgsConstructor
public class PsicologoController {

    private final PsicologoService psicologoService;

    // GET /api/psicologos          -> todos los activos o mensaje de no disponibilidad
    // GET /api/psicologos?modalidad=virtual  -> filtrado por modalidad
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String modalidad) {

        List<PsicologoDTO> lista = (modalidad != null && !modalidad.isBlank())
                ? psicologoService.listarPorModalidad(modalidad)
                : psicologoService.listarDisponibles();

        if (lista.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "No hay psicólogos disponibles en este momento. Por favor, intente más tarde.",
                    "psicologos", lista
            ));
        }

        return ResponseEntity.ok(lista);
    }

    // GET /api/psicologos/{id}     -> detalle del especialista incluyendo horarios disponibles
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Long id) {
        try {
            PsicologoDetalleDTO detalle = psicologoService.obtenerDetalle(id);
            return ResponseEntity.ok(detalle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}