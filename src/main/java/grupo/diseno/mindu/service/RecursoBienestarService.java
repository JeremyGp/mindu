package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.RecursoBienestarDTO;
import grupo.diseno.mindu.model.RecursoBienestar;
import grupo.diseno.mindu.model.TipoRecurso;
import grupo.diseno.mindu.repository.RecursoBienestarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecursoBienestarService {

    private final RecursoBienestarRepository recursoBienestarRepository;

    @Transactional(readOnly = true)
    public List<RecursoBienestarDTO> listarPorTipo(TipoRecurso tipo) {
        return recursoBienestarRepository.findByTipoOrderByOrdenVisualizacionAsc(tipo)
                .stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public RecursoBienestarDTO obtenerDetalle(Long id) {
        RecursoBienestar recurso = recursoBienestarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));
        return mapToDTO(recurso);
    }

    private RecursoBienestarDTO mapToDTO(RecursoBienestar r) {
        return RecursoBienestarDTO.builder()
                .id(r.getId())
                .tipo(r.getTipo())
                .titulo(r.getTitulo())
                .descripcion(r.getDescripcion())
                .duracionSegundos(r.getDuracionSegundos())
                .pasos(r.getPasos())
                .build();
    }
}