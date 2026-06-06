package grupo.diseno.mindu.service;

import grupo.diseno.mindu.dto.PsicologoDetalleDTO;
import grupo.diseno.mindu.model.Disponibilidad;
import grupo.diseno.mindu.model.Psicologo;
import grupo.diseno.mindu.model.Rol;
import grupo.diseno.mindu.repository.DisponibilidadRepository;
import grupo.diseno.mindu.repository.PsicologoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsicologoServiceTest {

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @InjectMocks
    private PsicologoService psicologoService;

    private Psicologo psicologo;
    private Disponibilidad disponibilidad;

    @BeforeEach
    void setUp() {
        psicologo = new Psicologo();
        psicologo.setId(1L);
        psicologo.setNombre("Carlos");
        psicologo.setApellido("Soto");
        psicologo.setCorreo("carlos.soto@mindu.com");
        psicologo.setActivo(true);
        psicologo.setEspecialidad("Estrés");
        psicologo.setModalidad("VIRTUAL");
        psicologo.setRol(Rol.PSICOLOGO);

        disponibilidad = Disponibilidad.builder()
                .id(100L)
                .psicologo(psicologo)
                .fecha(LocalDate.now().plusDays(1))
                .hora(LocalTime.of(15, 0))
                .disponible(true)
                .build();
    }

    @Test
    void obtenerDetalle_Exito() {
        // Arrange
        when(psicologoRepository.findById(1L)).thenReturn(Optional.of(psicologo));
        when(disponibilidadRepository.findByPsicologoIdAndDisponibleTrueAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(disponibilidad));

        // Act
        PsicologoDetalleDTO result = psicologoService.obtenerDetalle(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Carlos", result.getNombre());
        assertEquals("Soto", result.getApellido());
        assertEquals("Estrés", result.getEspecialidad());
        assertEquals("VIRTUAL", result.getModalidad());
        assertEquals("carlos.soto@mindu.com", result.getCorreo());
        assertFalse(result.getHorariosDisponibles().isEmpty());
        assertEquals(100L, result.getHorariosDisponibles().get(0).getId());
    }

    @Test
    void obtenerDetalle_Error_NoExiste() {
        // Arrange
        when(psicologoRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            psicologoService.obtenerDetalle(2L);
        });

        assertEquals("Psicólogo no encontrado", exception.getMessage());
    }

    @Test
    void obtenerDetalle_Error_Inactivo() {
        // Arrange
        psicologo.setActivo(false);
        when(psicologoRepository.findById(1L)).thenReturn(Optional.of(psicologo));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            psicologoService.obtenerDetalle(1L);
        });

        assertEquals("El psicólogo seleccionado no está activo", exception.getMessage());
    }
}
