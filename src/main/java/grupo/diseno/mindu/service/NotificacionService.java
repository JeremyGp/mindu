package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.NotificacionResponseDTO;
import grupo.diseno.mindu.model.Notificacion;
import grupo.diseno.mindu.model.TipoNotificacion;
import grupo.diseno.mindu.model.Usuario;
import grupo.diseno.mindu.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    // La usa el scheduler para generar notificaciones del sistema
    @Transactional
    public void crear(Usuario usuario, TipoNotificacion tipo, String titulo, String mensaje, Long referenciaId) {
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .referenciaId(referenciaId)
                .leida(false)
                .build();
        notificacionRepository.save(notificacion);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarMisNotificaciones(String correo) {
        return notificacionRepository.findByUsuarioCorreoOrderByCreadoEnDesc(correo)
                .stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String correo) {
        return notificacionRepository.countByUsuarioCorreoAndLeidaFalse(correo);
    }

    @Transactional
    public NotificacionResponseDTO marcarComoLeida(Long id, String correo) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));

        if (!notificacion.getUsuario().getCorreo().equals(correo)) {
            throw new SecurityException("No tienes permiso para modificar esta notificación");
        }
        notificacion.setLeida(true);
        return mapToDTO(notificacionRepository.save(notificacion));
    }

    @Transactional
    public void marcarTodasComoLeidas(String correo) {
        List<Notificacion> pendientes = notificacionRepository.findByUsuarioCorreoOrderByCreadoEnDesc(correo)
                .stream().filter(n -> !n.getLeida()).toList();
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }

    private NotificacionResponseDTO mapToDTO(Notificacion n) {
        return NotificacionResponseDTO.builder()
                .id(n.getId()).tipo(n.getTipo()).titulo(n.getTitulo())
                .mensaje(n.getMensaje()).leida(n.getLeida())
                .referenciaId(n.getReferenciaId()).creadoEn(n.getCreadoEn())
                .build();
    }
}