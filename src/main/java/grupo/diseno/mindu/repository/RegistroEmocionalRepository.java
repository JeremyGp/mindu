package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.RegistroEmocional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroEmocionalRepository extends JpaRepository<RegistroEmocional, Long> {

    Optional<RegistroEmocional> findByEstudianteIdAndFecha(Long estudianteId, LocalDate fecha);

    List<RegistroEmocional> findByEstudianteCorreoAndFechaGreaterThanEqualOrderByFechaAsc(
            String correo, LocalDate desde);

    List<RegistroEmocional> findTop30ByEstudianteCorreoOrderByFechaDesc(String correo);
}