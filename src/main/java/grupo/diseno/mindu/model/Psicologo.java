package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "psicologos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Psicologo extends Usuario {

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private String modalidad;

}