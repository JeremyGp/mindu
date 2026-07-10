package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_emocionales",
        uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "fecha"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEmocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAnimo estadoAnimo;

    @Column(length = 500)
    private String notas;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }
}