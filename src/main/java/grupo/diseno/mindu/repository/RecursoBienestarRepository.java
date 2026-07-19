package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.RecursoBienestar;
import grupo.diseno.mindu.model.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecursoBienestarRepository extends JpaRepository<RecursoBienestar, Long> {
    List<RecursoBienestar> findByTipoOrderByOrdenVisualizacionAsc(TipoRecurso tipo);
}