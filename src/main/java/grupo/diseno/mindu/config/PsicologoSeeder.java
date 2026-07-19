package grupo.diseno.mindu.config;

import grupo.diseno.mindu.model.Disponibilidad;
import grupo.diseno.mindu.model.Psicologo;
import grupo.diseno.mindu.model.Rol;
import grupo.diseno.mindu.repository.DisponibilidadRepository;
import grupo.diseno.mindu.repository.PsicologoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PsicologoSeeder implements CommandLineRunner {

    private final PsicologoRepository psicologoRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<Psicologo> psicologos = psicologoRepository.findAll();

        if (psicologos.isEmpty()) {
            psicologos = List.of(
                    psicologoRepository.save(crearPsicologo("Carla", "Ruiz", "carla.ruiz@mindu.pe", "Ansiedad y estrés académico", "virtual")),
                    psicologoRepository.save(crearPsicologo("Jorge", "Medina", "jorge.medina@mindu.pe", "Terapia cognitivo-conductual", "presencial")),
                    psicologoRepository.save(crearPsicologo("Valeria", "Soto", "valeria.soto@mindu.pe", "Manejo de crisis y regulación emocional", "virtual"))
            );
        }

        if (disponibilidadRepository.existsByFechaGreaterThanEqual(java.time.LocalDate.now())) return;

        for (Psicologo psicologo : psicologos) {
            generarDisponibilidad(psicologo);
        }
    }

    private Psicologo crearPsicologo(String nombre, String apellido, String correo, String especialidad, String modalidad) {
        Psicologo psicologo = new Psicologo();
        psicologo.setNombre(nombre);
        psicologo.setApellido(apellido);
        psicologo.setCorreo(correo);
        psicologo.setPassword(passwordEncoder.encode("mindu123"));
        psicologo.setRol(Rol.PSICOLOGO);
        psicologo.setActivo(true);
        psicologo.setFechaRegistro(LocalDateTime.now());
        psicologo.setEspecialidad(especialidad);
        psicologo.setModalidad(modalidad);
        return psicologo;
    }

    private void generarDisponibilidad(Psicologo psicologo) {
        List<LocalTime> horas = List.of(LocalTime.of(9, 0), LocalTime.of(11, 0), LocalTime.of(15, 0), LocalTime.of(17, 0));

        for (int dia = 1; dia <= 10; dia++) {
            LocalDate fecha = LocalDate.now().plusDays(dia);
            for (LocalTime hora : horas) {
                disponibilidadRepository.save(Disponibilidad.builder()
                        .psicologo(psicologo)
                        .fecha(fecha)
                        .hora(hora)
                        .disponible(true)
                        .build());
            }
        }
    }
}