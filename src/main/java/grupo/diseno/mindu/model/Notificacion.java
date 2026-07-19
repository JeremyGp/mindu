package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida;

    private Long referenciaId; // ej: id de la cita relacionada, para enlazar/deduplicar

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (leida == null) leida = false;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}