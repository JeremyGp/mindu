package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByPsicologoIdAndDisponibleTrueAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
            Long psicologoId, LocalDate fecha);

    Optional<Disponibilidad> findByPsicologoIdAndFechaAndHora(
            Long psicologoId, LocalDate fecha, LocalTime hora);
}
