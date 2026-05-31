package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estudiantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Estudiante extends Usuario {

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false)
    private Integer cicloAcademico;

}