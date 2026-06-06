package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.ActualizarPerfilRequest;
import grupo.diseno.mindu.dto.AuthResponse;
import grupo.diseno.mindu.dto.LoginRequest;
import grupo.diseno.mindu.dto.PerfilResponseDTO;
import grupo.diseno.mindu.dto.RegistroRequest;
import grupo.diseno.mindu.model.Estudiante;
import grupo.diseno.mindu.model.Psicologo;
import grupo.diseno.mindu.model.Rol;
import grupo.diseno.mindu.model.Usuario;
import grupo.diseno.mindu.repository.EstudianteRepository;
import grupo.diseno.mindu.repository.PsicologoRepository;
import grupo.diseno.mindu.repository.UsuarioRepository;
import grupo.diseno.mindu.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PsicologoRepository psicologoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public String registrar(RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya se encuentra en uso");
        }
        if (estudianteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }
        if (estudianteRepository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("El código universitario ya está registrado");
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.getNombre());
        estudiante.setApellido(request.getApellido());
        estudiante.setCorreo(request.getCorreo());
        estudiante.setPassword(passwordEncoder.encode(request.getPassword()));
        estudiante.setDni(request.getDni());
        estudiante.setCodigo(request.getCodigo());
        estudiante.setEdad(request.getEdad());
        estudiante.setCicloAcademico(request.getCicloAcademico());
        estudiante.setRol(Rol.ESTUDIANTE);
        estudiante.setActivo(true);
        estudiante.setFechaRegistro(LocalDateTime.now());

        estudianteRepository.save(estudiante);
        return "Cuenta creada exitosamente";
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        // Verificar si la cuenta está temporalmente bloqueada
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La cuenta está temporalmente bloqueada por demasiados intentos fallidos. Intente de nuevo más tarde.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
            usuario.setIntentosFallidos(intentos + 1);
            if (usuario.getIntentosFallidos() >= 5) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
            }
            usuarioRepository.save(usuario);
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("La cuenta está desactivada");
        }

        // Restablecer intentos fallidos tras inicio de sesión exitoso
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol().name());
        return new AuthResponse(token, usuario.getNombre(), usuario.getCorreo(), usuario.getRol().name());
    }

    @Transactional(readOnly = true)
    public PerfilResponseDTO obtenerPerfil(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return mapToPerfilDTO(usuario);
    }

    @Transactional
    public PerfilResponseDTO actualizarPerfil(String correo, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (usuario.getRol() == Rol.ESTUDIANTE) {
            Estudiante estudiante = estudianteRepository.findByCorreo(correo)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
            
            estudiante.setNombre(usuario.getNombre());
            estudiante.setApellido(usuario.getApellido());
            estudiante.setPassword(usuario.getPassword());

            if (request.getEdad() != null) {
                if (request.getEdad() < 16) {
                    throw new IllegalArgumentException("La edad mínima es 16 años");
                }
                estudiante.setEdad(request.getEdad());
            }

            if (request.getCicloAcademico() != null) {
                if (request.getCicloAcademico() < 1 || request.getCicloAcademico() > 12) {
                    throw new IllegalArgumentException("El ciclo académico debe estar entre 1 y 12");
                }
                estudiante.setCicloAcademico(request.getCicloAcademico());
            }

            Estudiante saved = estudianteRepository.save(estudiante);
            return mapToPerfilDTO(saved);

        } else if (usuario.getRol() == Rol.PSICOLOGO) {
            Psicologo psicologo = psicologoRepository.findByCorreo(correo)
                    .orElseThrow(() -> new IllegalArgumentException("Psicólogo no encontrado"));

            psicologo.setNombre(usuario.getNombre());
            psicologo.setApellido(usuario.getApellido());
            psicologo.setPassword(usuario.getPassword());

            if (request.getEspecialidad() != null && !request.getEspecialidad().isBlank()) {
                psicologo.setEspecialidad(request.getEspecialidad());
            }

            if (request.getModalidad() != null && !request.getModalidad().isBlank()) {
                psicologo.setModalidad(request.getModalidad());
            }

            Psicologo saved = psicologoRepository.save(psicologo);
            return mapToPerfilDTO(saved);
        }

        Usuario saved = usuarioRepository.save(usuario);
        return mapToPerfilDTO(saved);
    }

    private PerfilResponseDTO mapToPerfilDTO(Usuario usuario) {
        PerfilResponseDTO.PerfilResponseDTOBuilder builder = PerfilResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().name());

        if (usuario instanceof Estudiante estudiante) {
            builder.dni(estudiante.getDni())
                    .codigo(estudiante.getCodigo())
                    .edad(estudiante.getEdad())
                    .cicloAcademico(estudiante.getCicloAcademico());
        } else if (usuario instanceof Psicologo psicologo) {
            builder.especialidad(psicologo.getEspecialidad())
                    .modalidad(psicologo.getModalidad());
        }

        return builder.build();
    }
}
