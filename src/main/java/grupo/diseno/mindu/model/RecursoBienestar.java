package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "recursos_bienestar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoBienestar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecurso tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer duracionSegundos;

    @ElementCollection
    @CollectionTable(name = "recursos_bienestar_pasos", joinColumns = @JoinColumn(name = "recurso_id"))
    @Column(name = "instruccion", length = 300)
    @OrderColumn(name = "orden")
    private List<String> pasos;

    @Column(nullable = false)
    private Integer ordenVisualizacion;
}