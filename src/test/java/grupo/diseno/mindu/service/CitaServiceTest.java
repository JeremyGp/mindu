package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.AgendarCitaRequest;
import grupo.diseno.mindu.dto.CitaResponseDTO;
import grupo.diseno.mindu.model.*;
import grupo.diseno.mindu.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @InjectMocks
    private CitaService citaService;

    private Estudiante estudiante;
    private Psicologo psicologo;
    private Disponibilidad disponibilidad;
    private AgendarCitaRequest request;

    @BeforeEach
    void setUp() {
        estudiante = new Estudiante();
        estudiante.setId(1L);
        estudiante.setNombre("Juan");
        estudiante.setApellido("Perez");
        estudiante.setCorreo("juan.perez@universidad.edu.pe");
        estudiante.setActivo(true);
        estudiante.setRol(Rol.ESTUDIANTE);

        psicologo = new Psicologo();
        psicologo.setId(2L);
        psicologo.setNombre("Ana");
        psicologo.setApellido("Gomez");
        psicologo.setCorreo("ana.gomez@mindu.com");
        psicologo.setActivo(true);
        psicologo.setEspecialidad("Ansiedad");
        psicologo.setRol(Rol.PSICOLOGO);

        disponibilidad = Disponibilidad.builder()
                .id(100L)
                .psicologo(psicologo)
                .fecha(LocalDate.now().plusDays(2))
                .hora(LocalTime.of(10, 0))
                .disponible(true)
                .build();

        request = new AgendarCitaRequest();
        request.setPsicologoId(2L);
        request.setFecha(LocalDate.now().plusDays(2));
        request.setHora(LocalTime.of(10, 0));
        request.setModalidad("VIRTUAL");
    }

    @Test
    void agendarCita_Exito() {
        // Arrange
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(psicologoRepository.findById(request.getPsicologoId())).thenReturn(Optional.of(psicologo));
        when(disponibilidadRepository.findByPsicologoIdAndFechaAndHora(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()))).thenReturn(Optional.of(disponibilidad));
        when(citaRepository.existsOverlappingPsicologo(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()), eq(EstadoCita.CANCELADA))).thenReturn(false);
        when(citaRepository.existsOverlappingEstudiante(eq(estudiante.getId()), eq(request.getFecha()), eq(request.getHora()), eq(EstadoCita.CANCELADA))).thenReturn(false);

        Cita savedCita = Cita.builder()
                .id(10L)
                .fecha(request.getFecha())
                .hora(request.getHora())
                .modalidad(request.getModalidad())
                .estudiante(estudiante)
                .psicologo(psicologo)
                .estado(EstadoCita.PENDIENTE)
                .build();
        when(citaRepository.save(any(Cita.class))).thenReturn(savedCita);

        // Act
        CitaResponseDTO result = citaService.agendarCita(request, estudiante.getCorreo());

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(EstadoCita.PENDIENTE, result.getEstado());
        assertEquals("Juan Perez", result.getEstudianteNombreCompleto());
        assertEquals("Ana Gomez", result.getPsicologoNombreCompleto());
        assertFalse(disponibilidad.getDisponible()); // Debe haberse marcado como no disponible (reservada)
        verify(disponibilidadRepository, times(1)).save(disponibilidad);
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    void agendarCita_Error_NoDisponibilidad() {
        // Arrange
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(psicologoRepository.findById(request.getPsicologoId())).thenReturn(Optional.of(psicologo));
        when(disponibilidadRepository.findByPsicologoIdAndFechaAndHora(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            citaService.agendarCita(request, estudiante.getCorreo());
        });

        assertEquals("El psicólogo no tiene disponibilidad configurada para esta fecha y hora", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void agendarCita_Error_DisponibilidadYaReservada() {
        // Arrange
        disponibilidad.setDisponible(false);
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(psicologoRepository.findById(request.getPsicologoId())).thenReturn(Optional.of(psicologo));
        when(disponibilidadRepository.findByPsicologoIdAndFechaAndHora(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()))).thenReturn(Optional.of(disponibilidad));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            citaService.agendarCita(request, estudiante.getCorreo());
        });

        assertEquals("El horario seleccionado ya no está disponible", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void agendarCita_Error_PsicologoOcupado() {
        // Arrange
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(psicologoRepository.findById(request.getPsicologoId())).thenReturn(Optional.of(psicologo));
        when(disponibilidadRepository.findByPsicologoIdAndFechaAndHora(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()))).thenReturn(Optional.of(disponibilidad));
        when(citaRepository.existsOverlappingPsicologo(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()), eq(EstadoCita.CANCELADA))).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            citaService.agendarCita(request, estudiante.getCorreo());
        });

        assertEquals("El psicólogo ya tiene una cita programada para esta fecha y hora", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void agendarCita_Error_EstudianteOcupado() {
        // Arrange
        when(estudianteRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(psicologoRepository.findById(request.getPsicologoId())).thenReturn(Optional.of(psicologo));
        when(disponibilidadRepository.findByPsicologoIdAndFechaAndHora(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()))).thenReturn(Optional.of(disponibilidad));
        when(citaRepository.existsOverlappingPsicologo(eq(psicologo.getId()), eq(request.getFecha()), eq(request.getHora()), eq(EstadoCita.CANCELADA))).thenReturn(false);
        when(citaRepository.existsOverlappingEstudiante(eq(estudiante.getId()), eq(request.getFecha()), eq(request.getHora()), eq(EstadoCita.CANCELADA))).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            citaService.agendarCita(request, estudiante.getCorreo());
        });

        assertEquals("Ya tienes otra cita programada para esta misma fecha y hora", exception.getMessage());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void obtenerCitaDetalle_Exito() {
        // Arrange
        Cita cita = Cita.builder()
                .id(50L)
                .fecha(LocalDate.now())
                .hora(LocalTime.of(14, 0))
                .estudiante(estudiante)
                .psicologo(psicologo)
                .estado(EstadoCita.CONFIRMADA)
                .build();
        when(usuarioRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(citaRepository.findById(50L)).thenReturn(Optional.of(cita));

        // Act
        CitaResponseDTO result = citaService.obtenerCitaDetalle(50L, estudiante.getCorreo());

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("Juan Perez", result.getEstudianteNombreCompleto());
    }

    @Test
    void obtenerCitaDetalle_Error_NoPermiso() {
        // Arrange
        Estudiante otroEstudiante = new Estudiante();
        otroEstudiante.setId(99L);
        otroEstudiante.setCorreo("otro.estudiante@universidad.edu.pe");

        Cita cita = Cita.builder()
                .id(50L)
                .estudiante(otroEstudiante)
                .psicologo(psicologo)
                .build();

        when(usuarioRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(citaRepository.findById(50L)).thenReturn(Optional.of(cita));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            citaService.obtenerCitaDetalle(50L, estudiante.getCorreo());
        });
    }

    @Test
    void obtenerCitaDetalle_Error_NoExiste() {
        // Arrange
        when(usuarioRepository.findByCorreo(estudiante.getCorreo())).thenReturn(Optional.of(estudiante));
        when(citaRepository.findById(50L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            citaService.obtenerCitaDetalle(50L, estudiante.getCorreo());
        });
    }
}
