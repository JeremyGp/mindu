package grupo.diseno.mindu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected String nombre;

    @Column(nullable = false)
    protected String apellido;

    @Column(nullable = false, unique = true)
    protected String correo;

    @Column(nullable = false)
    protected String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected Rol rol;

    @Column(nullable = false)
    protected Boolean activo;

    @Column(nullable = false)
    protected LocalDateTime fechaRegistro;

    @Column(nullable = false, columnDefinition = "integer default 0")
    protected Integer intentosFallidos = 0;

    @Column
    protected LocalDateTime bloqueadoHasta;

}
