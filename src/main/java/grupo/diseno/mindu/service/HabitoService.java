package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.DiaHabitoDTO;
import grupo.diseno.mindu.dto.RegistroHabitoRequest;
import grupo.diseno.mindu.dto.RegistroHabitoResponseDTO;
import grupo.diseno.mindu.model.Estudiante;
import grupo.diseno.mindu.model.RegistroHabito;
import grupo.diseno.mindu.repository.EstudianteRepository;
import grupo.diseno.mindu.repository.RegistroHabitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HabitoService {

    private static final String[] DIAS_ABREVIADOS = {"L", "M", "X", "J", "V", "S", "D"};

    private final RegistroHabitoRepository registroHabitoRepository;
    private final EstudianteRepository estudianteRepository;

    @Transactional
    public RegistroHabitoResponseDTO actualizarHabitoHoy(RegistroHabitoRequest request, String estudianteCorreo) {
        Estudiante estudiante = estudianteRepository.findByCorreo(estudianteCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        if (!estudiante.getActivo()) {
            throw new IllegalArgumentException("La cuenta del estudiante está inactiva");
        }

        LocalDate hoy = LocalDate.now();

        RegistroHabito registro = registroHabitoRepository
                .findByEstudianteIdAndFecha(estudiante.getId(), hoy)
                .orElse(RegistroHabito.builder()
                        .estudiante(estudiante)
                        .fecha(hoy)
                        .desayuno(false)
                        .almuerzo(false)
                        .cena(false)
                        .vasosAgua(0)
                        .pasos(0)
                        .metaPasos(8000)
                        .build());

        // Actualización parcial: cada widget (sueño, comidas, hidratación, actividad) envía solo su campo
        if (request.getHorasSueno() != null) registro.setHorasSueno(request.getHorasSueno());
        if (request.getCalidadSueno() != null) registro.setCalidadSueno(request.getCalidadSueno());
        if (request.getDesayuno() != null) registro.setDesayuno(request.getDesayuno());
        if (request.getAlmuerzo() != null) registro.setAlmuerzo(request.getAlmuerzo());
        if (request.getCena() != null) registro.setCena(request.getCena());
        if (request.getVasosAgua() != null) registro.setVasosAgua(request.getVasosAgua());
        if (request.getPasos() != null) registro.setPasos(request.getPasos());
        if (request.getMetaPasos() != null) registro.setMetaPasos(request.getMetaPasos());

        RegistroHabito guardado = registroHabitoRepository.save(registro);
        return mapToDTO(guardado);
    }

    @Transactional(readOnly = true)
    public RegistroHabitoResponseDTO obtenerHabitoDeHoy(String estudianteCorreo) {
        Estudiante estudiante = estudianteRepository.findByCorreo(estudianteCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        return registroHabitoRepository.findByEstudianteIdAndFecha(estudiante.getId(), LocalDate.now())
                .map(this::mapToDTO)
                .orElse(RegistroHabitoResponseDTO.builder()
                        .fecha(LocalDate.now())
                        .desayuno(false)
                        .almuerzo(false)
                        .cena(false)
                        .vasosAgua(0)
                        .pasos(0)
                        .metaPasos(8000)
                        .build());
    }

    @Transactional(readOnly = true)
    public List<DiaHabitoDTO> obtenerResumenSemanal(String estudianteCorreo) {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        List<RegistroHabito> registros = registroHabitoRepository
                .findByEstudianteCorreoAndFechaBetweenOrderByFechaAsc(estudianteCorreo, lunes, domingo);

        Map<LocalDate, RegistroHabito> porFecha = new HashMap<>();
        registros.forEach(r -> porFecha.put(r.getFecha(), r));

        List<DiaHabitoDTO> resumen = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = lunes.plusDays(i);
            resumen.add(DiaHabitoDTO.builder()
                    .fecha(dia)
                    .diaAbreviado(DIAS_ABREVIADOS[i])
                    .registrado(porFecha.containsKey(dia))
                    .build());
        }
        return resumen;
    }

    private RegistroHabitoResponseDTO mapToDTO(RegistroHabito registro) {
        return RegistroHabitoResponseDTO.builder()
                .id(registro.getId())
                .fecha(registro.getFecha())
                .horasSueno(registro.getHorasSueno())
                .calidadSueno(registro.getCalidadSueno())
                .desayuno(registro.getDesayuno())
                .almuerzo(registro.getAlmuerzo())
                .cena(registro.getCena())
                .vasosAgua(registro.getVasosAgua())
                .pasos(registro.getPasos())
                .metaPasos(registro.getMetaPasos())
                .build();
    }
}