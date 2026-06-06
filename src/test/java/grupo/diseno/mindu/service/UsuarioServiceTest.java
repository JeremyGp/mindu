package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.ActualizarPerfilRequest;
import grupo.diseno.mindu.dto.AuthResponse;
import grupo.diseno.mindu.dto.LoginRequest;
import grupo.diseno.mindu.dto.PerfilResponseDTO;
import grupo.diseno.mindu.model.Estudiante;
import grupo.diseno.mindu.model.Rol;
import grupo.diseno.mindu.repository.EstudianteRepository;
import grupo.diseno.mindu.repository.PsicologoRepository;
import grupo.diseno.mindu.repository.UsuarioRepository;
import grupo.diseno.mindu.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    private Estudiante estudiante;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        estudiante = new Estudiante();
        estudiante.setId(10L);
        estudiante.setNombre("Mario");
        estudiante.setApellido("Vargas");
        estudiante.setCorreo("mario.vargas@universidad.edu.pe");
        estudiante.setPassword("cryptedPassword");
        estudiante.setActivo(true);
        estudiante.setRol(Rol.ESTUDIANTE);
        estudiante.setIntentosFallidos(0);
        estudiante.setBloqueadoHasta(null);
        estudiante.setEdad(20);
        estudiante.setCicloAcademico(4);

        loginRequest = new LoginRequest();
        loginRequest.setCorreo("mario.vargas@universidad.edu.pe");
        loginRequest.setPassword("myPassword");
    }

    @Test
    void login_Exito_RestableceIntentos() {
        // Arrange
        estudiante.setIntentosFallidos(3); // Simular intentos fallidos previos
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(estudiante));
        when(passwordEncoder.matches(loginRequest.getPassword(), estudiante.getPassword())).thenReturn(true);
        when(jwtUtil.generarToken(estudiante.getCorreo(), estudiante.getRol().name())).thenReturn("validToken");

        // Act
        AuthResponse result = usuarioService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("validToken", result.getToken());
        assertEquals(0, estudiante.getIntentosFallidos()); // Debe restablecerse a 0
        assertNull(estudiante.getBloqueadoHasta());
        verify(usuarioRepository, times(1)).save(estudiante);
    }

    @Test
    void login_Error_IncrementaIntentos() {
        // Arrange
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(estudiante));
        when(passwordEncoder.matches(loginRequest.getPassword(), estudiante.getPassword())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.login(loginRequest);
        });

        assertEquals("Credenciales incorrectas", exception.getMessage());
        assertEquals(1, estudiante.getIntentosFallidos()); // Se incrementó
        assertNull(estudiante.getBloqueadoHasta());
        verify(usuarioRepository, times(1)).save(estudiante);
    }

    @Test
    void login_Error_BloqueaCuenta_AlQuintoIntento() {
        // Arrange
        estudiante.setIntentosFallidos(4); // Ya tiene 4 intentos fallidos
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(estudiante));
        when(passwordEncoder.matches(loginRequest.getPassword(), estudiante.getPassword())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.login(loginRequest);
        });

        assertEquals("Credenciales incorrectas", exception.getMessage());
        assertEquals(5, estudiante.getIntentosFallidos());
        assertNotNull(estudiante.getBloqueadoHasta()); // Ahora debe estar bloqueado
        assertTrue(estudiante.getBloqueadoHasta().isAfter(LocalDateTime.now()));
        verify(usuarioRepository, times(1)).save(estudiante);
    }

    @Test
    void login_Error_CuentaBloquedaTemporalmente() {
        // Arrange
        estudiante.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10)); // Cuenta bloqueada
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(estudiante));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("La cuenta está temporalmente bloqueada"));
        verify(passwordEncoder, never()).matches(anyString(), anyString()); // No debe verificar contraseña si está bloqueada
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void obtenerPerfil_Exito() {
        // Arrange
        when(usuarioRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));

        // Act
        PerfilResponseDTO result = usuarioService.obtenerPerfil(estudiante.getCorreo());

        // Assert
        assertNotNull(result);
        assertEquals("Mario", result.getNombre());
        assertEquals("Vargas", result.getApellido());
        assertEquals(Rol.ESTUDIANTE.name(), result.getRol());
        assertEquals(20, result.getEdad());
    }

    @Test
    void actualizarPerfil_Estudiante_Exito() {
        // Arrange
        ActualizarPerfilRequest updateReq = new ActualizarPerfilRequest();
        updateReq.setNombre("Mario Alberto");
        updateReq.setApellido("Vargas Llosa");
        updateReq.setPassword("newSecurePassword");
        updateReq.setEdad(21);
        updateReq.setCicloAcademico(5);

        when(usuarioRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(passwordEncoder.encode("newSecurePassword")).thenReturn("newCryptedPassword");
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PerfilResponseDTO result = usuarioService.actualizarPerfil(estudiante.getCorreo(), updateReq);

        // Assert
        assertNotNull(result);
        assertEquals("Mario Alberto", result.getNombre());
        assertEquals("Vargas Llosa", result.getApellido());
        assertEquals(21, result.getEdad());
        assertEquals(5, result.getCicloAcademico());
        assertEquals("newCryptedPassword", estudiante.getPassword());
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    @Test
    void actualizarPerfil_Estudiante_Error_EdadMinima() {
        // Arrange
        ActualizarPerfilRequest updateReq = new ActualizarPerfilRequest();
        updateReq.setNombre("Mario");
        updateReq.setApellido("Vargas");
        updateReq.setEdad(15); // Inválido (menor a 16)

        when(usuarioRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.actualizarPerfil(estudiante.getCorreo(), updateReq);
        });
        verify(estudianteRepository, never()).save(any());
    }
}
