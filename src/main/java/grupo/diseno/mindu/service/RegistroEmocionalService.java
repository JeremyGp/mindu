package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.*;
import grupo.diseno.mindu.model.EstadoAnimo;
import grupo.diseno.mindu.model.Estudiante;
import grupo.diseno.mindu.model.RegistroEmocional;
import grupo.diseno.mindu.repository.EstudianteRepository;
import grupo.diseno.mindu.repository.RegistroEmocionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroEmocionalService {

    private final RegistroEmocionalRepository registroEmocionalRepository;
    private final EstudianteRepository estudianteRepository;

    @Transactional
    public RegistroEmocionalResponseDTO registrarEstado(RegistroEmocionalRequest request, String estudianteCorreo) {
        Estudiante estudiante = estudianteRepository.findByCorreo(estudianteCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        if (!estudiante.getActivo()) {
            throw new IllegalArgumentException("La cuenta del estudiante está inactiva");
        }

        LocalDate fecha = request.getFecha() != null ? request.getFecha() : LocalDate.now();

        // HU007: un registro por estudiante y día; si ya existe, se actualiza (evita duplicados)
        RegistroEmocional registro = registroEmocionalRepository
                .findByEstudianteIdAndFecha(estudiante.getId(), fecha)
                .orElse(RegistroEmocional.builder()
                        .estudiante(estudiante)
                        .fecha(fecha)
                        .build());

        registro.setEstadoAnimo(request.getEstadoAnimo());
        registro.setNotas(request.getNotas());

        RegistroEmocional guardado = registroEmocionalRepository.save(registro);
        return mapToDTO(guardado);
    }

    @Transactional(readOnly = true)
    public RegistroEmocionalResponseDTO obtenerRegistroDeHoy(String estudianteCorreo) {
        Estudiante estudiante = estudianteRepository.findByCorreo(estudianteCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        return registroEmocionalRepository.findByEstudianteIdAndFecha(estudiante.getId(), LocalDate.now())
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public EvolucionEmocionalDTO obtenerEvolucion(String estudianteCorreo, int dias) {
        // HU018: la consulta siempre se filtra por el correo autenticado (JWT), nunca por otro usuario
        LocalDate desde = LocalDate.now().minusDays(Math.max(dias, 1) - 1L);

        List<RegistroEmocional> registros = registroEmocionalRepository
                .findByEstudianteCorreoAndFechaGreaterThanEqualOrderByFechaAsc(estudianteCorreo, desde);

        List<RegistroEmocionalResponseDTO> dtos = registros.stream()
                .map(this::mapToDTO)
                .toList();

        double promedio = registros.stream()
                .mapToInt(r -> r.getEstadoAnimo().getValor())
                .average()
                .orElse(0.0);

        String tendencia = calcularTendencia(registros);

        return EvolucionEmocionalDTO.builder()
                .registros(dtos)
                .promedio(Math.round(promedio * 100.0) / 100.0)
                .tendencia(tendencia)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecomendacionBienestarDTO> obtenerRecomendaciones(String estudianteCorreo) {
        List<RegistroEmocional> ultimos = registroEmocionalRepository
                .findTop30ByEstudianteCorreoOrderByFechaDesc(estudianteCorreo);

        List<RecomendacionBienestarDTO> recomendaciones = new ArrayList<>();

        if (ultimos.isEmpty()) {
            recomendaciones.add(RecomendacionBienestarDTO.builder()
                    .categoria("Bienestar")
                    .mensaje("Aún no tienes registros emocionales. Empieza hoy a registrar tu estado de ánimo para recibir recomendaciones personalizadas.")
                    .nivel("INFO")
                    .build());
            return recomendaciones;
        }

        // HU009: reglas simples sobre el historial reciente (sin depender de servicios externos)
        List<RegistroEmocional> ultimosTres = ultimos.stream().limit(3).toList();
        double promedioReciente = ultimosTres.stream()
                .mapToInt(r -> r.getEstadoAnimo().getValor())
                .average()
                .orElse(3.0);

        long diasBajos = ultimosTres.stream()
                .filter(r -> r.getEstadoAnimo() == EstadoAnimo.MUY_MAL || r.getEstadoAnimo() == EstadoAnimo.MAL)
                .count();

        if (diasBajos >= 3) {
            recomendaciones.add(RecomendacionBienestarDTO.builder()
                    .categoria("Apoyo profesional")
                    .mensaje("Has registrado un estado de ánimo bajo en los últimos días. Considera agendar una cita con un psicólogo de MindU para conversarlo.")
                    .nivel("ALERTA")
                    .build());
        } else if (diasBajos >= 1) {
            recomendaciones.add(RecomendacionBienestarDTO.builder()
                    .categoria("Autocuidado")
                    .mensaje("Notamos algunos días difíciles recientemente. Prueba una pausa de respiración de 5 minutos o una caminata corta al aire libre.")
                    .nivel("INFO")
                    .build());
        } else if (promedioReciente >= 4) {
            recomendaciones.add(RecomendacionBienestarDTO.builder()
                    .categoria("Bienestar")
                    .mensaje("¡Tu estado de ánimo se ha mantenido positivo! Sigue con tus hábitos actuales de sueño, alimentación y actividad física.")
                    .nivel("INFO")
                    .build());
        } else {
            recomendaciones.add(RecomendacionBienestarDTO.builder()
                    .categoria("Bienestar")
                    .mensaje("Tu estado de ánimo se mantiene estable. Recuerda registrar tus emociones diariamente para detectar cambios a tiempo.")
                    .nivel("INFO")
                    .build());
        }

        recomendaciones.add(RecomendacionBienestarDTO.builder()
                .categoria("Hábitos saludables")
                .mensaje("Dormir al menos 7 horas y mantener una rutina de hidratación ayuda a regular tus emociones. Revisa tu sección de Hábitos.")
                .nivel("INFO")
                .build());

        return recomendaciones;
    }

    private String calcularTendencia(List<RegistroEmocional> registrosAsc) {
        if (registrosAsc.size() < 2) {
            return "ESTABLE";
        }
        int mitad = registrosAsc.size() / 2;
        double promedioPrimeraMitad = registrosAsc.subList(0, mitad).stream()
                .mapToInt(r -> r.getEstadoAnimo().getValor())
                .average()
                .orElse(0.0);
        double promedioSegundaMitad = registrosAsc.subList(mitad, registrosAsc.size()).stream()
                .mapToInt(r -> r.getEstadoAnimo().getValor())
                .average()
                .orElse(0.0);

        double diferencia = promedioSegundaMitad - promedioPrimeraMitad;
        if (diferencia > 0.3) {
            return "MEJORANDO";
        } else if (diferencia < -0.3) {
            return "EMPEORANDO";
        }
        return "ESTABLE";
    }

    private RegistroEmocionalResponseDTO mapToDTO(RegistroEmocional registro) {
        return RegistroEmocionalResponseDTO.builder()
                .id(registro.getId())
                .fecha(registro.getFecha())
                .estadoAnimo(registro.getEstadoAnimo())
                .estadoAnimoEtiqueta(registro.getEstadoAnimo().getEtiqueta())
                .valorNumerico(registro.getEstadoAnimo().getValor())
                .notas(registro.getNotas())
                .creadoEn(registro.getCreadoEn())
                .build();
    }
}