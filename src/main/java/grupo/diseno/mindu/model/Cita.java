package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private String modalidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    @ManyToOne
    @JoinColumn(name="estudiante_id")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name="psicologo_id")
    private Psicologo psicologo;

    @PrePersist
    public void prePersist() {
        estado = EstadoCita.PENDIENTE;
    }
}

