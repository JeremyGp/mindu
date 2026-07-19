package grupo.diseno.mindu.repository;

import grupo.diseno.mindu.model.Notificacion;
import grupo.diseno.mindu.model.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioCorreoOrderByCreadoEnDesc(String correo);

    long countByUsuarioCorreoAndLeidaFalse(String correo);

    boolean existsByUsuarioIdAndTipoAndCreadoEnBetween(
            Long usuarioId, TipoNotificacion tipo, LocalDateTime desde, LocalDateTime hasta);
}