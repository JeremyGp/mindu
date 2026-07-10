package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_habitos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "fecha"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroHabito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private LocalDate fecha;

    private Double horasSueno;

    @Enumerated(EnumType.STRING)
    private CalidadSueno calidadSueno;

    @Column(nullable = false)
    private Boolean desayuno;

    @Column(nullable = false)
    private Boolean almuerzo;

    @Column(nullable = false)
    private Boolean cena;

    @Column(nullable = false)
    private Integer vasosAgua;

    @Column(nullable = false)
    private Integer pasos;

    @Column(nullable = false)
    private Integer metaPasos;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (fecha == null) fecha = LocalDate.now();
        if (desayuno == null) desayuno = false;
        if (almuerzo == null) almuerzo = false;
        if (cena == null) cena = false;
        if (vasosAgua == null) vasosAgua = 0;
        if (pasos == null) pasos = 0;
        if (metaPasos == null) metaPasos = 8000;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}