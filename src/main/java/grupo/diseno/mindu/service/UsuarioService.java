package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.AuthResponse;
import grupo.diseno.mindu.dto.LoginRequest;
import grupo.diseno.mindu.dto.RegistroRequest;
import grupo.diseno.mindu.model.Estudiante;
import grupo.diseno.mindu.model.Rol;
import grupo.diseno.mindu.model.Usuario;
import grupo.diseno.mindu.repository.EstudianteRepository;
import grupo.diseno.mindu.repository.UsuarioRepository;
import grupo.diseno.mindu.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
}
