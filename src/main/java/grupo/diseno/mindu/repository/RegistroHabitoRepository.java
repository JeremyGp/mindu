package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.RegistroHabito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {

    Optional<RegistroHabito> findByEstudianteIdAndFecha(Long estudianteId, LocalDate fecha);

    List<RegistroHabito> findByEstudianteCorreoAndFechaBetweenOrderByFechaAsc(
            String correo, LocalDate desde, LocalDate hasta);
}