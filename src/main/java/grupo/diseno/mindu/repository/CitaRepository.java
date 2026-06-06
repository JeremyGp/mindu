package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.Cita;
import grupo.diseno.mindu.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByEstudianteCorreoOrderByFechaAscHoraAsc(String correo);

    List<Cita> findByPsicologoCorreoOrderByFechaAscHoraAsc(String correo);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.psicologo.id = :psicologoId AND c.fecha = :fecha AND c.hora = :hora AND c.estado <> :estadoExcluido")
    boolean existsOverlappingPsicologo(
            @Param("psicologoId") Long psicologoId,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("estadoExcluido") EstadoCita estadoExcluido
    );

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.estudiante.id = :estudianteId AND c.fecha = :fecha AND c.hora = :hora AND c.estado <> :estadoExcluido")
    boolean existsOverlappingEstudiante(
            @Param("estudianteId") Long estudianteId,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("estadoExcluido") EstadoCita estadoExcluido
    );
}
