package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    boolean existsByDni(String dni);
    boolean existsByCodigo(String codigo);
    Optional<Estudiante> findByCorreo(String correo);
}
