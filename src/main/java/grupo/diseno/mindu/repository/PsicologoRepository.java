package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.Psicologo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, Long> {
    List<Psicologo> findByActivoTrue();
    List<Psicologo> findByActivoTrueAndModalidad(String modalidad);
    Optional<Psicologo> findByCorreo(String correo);
}