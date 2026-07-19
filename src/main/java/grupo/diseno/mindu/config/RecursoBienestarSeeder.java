package grupo.diseno.mindu.config;

import grupo.diseno.mindu.model.RecursoBienestar;
import grupo.diseno.mindu.model.TipoRecurso;
import grupo.diseno.mindu.repository.RecursoBienestarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecursoBienestarSeeder implements CommandLineRunner {

    private final RecursoBienestarRepository recursoBienestarRepository;

    @Override
    public void run(String... args) {
        if (recursoBienestarRepository.count() > 0) return;

        // ── RESPIRACIÓN: se toman su tiempo, cada una con 3 repeticiones ──
        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.RESPIRACION)
                .titulo("Respiración 4-7-8")
                .descripcion("Técnica clásica para calmar el sistema nervioso antes de un examen. 3 repeticiones.")
                .duracionSegundos(57)
                .ordenVisualizacion(1)
                .pasos(List.of(
                        "Repetición 1: Inhala por la nariz contando 4 segundos",
                        "Repetición 1: Sostén el aire contando 7 segundos",
                        "Repetición 1: Exhala lento por la boca contando 8 segundos",
                        "Repetición 2: Inhala por la nariz contando 4 segundos",
                        "Repetición 2: Sostén el aire contando 7 segundos",
                        "Repetición 2: Exhala lento por la boca contando 8 segundos",
                        "Repetición 3: Inhala por la nariz contando 4 segundos",
                        "Repetición 3: Sostén el aire contando 7 segundos",
                        "Repetición 3: Exhala lento por la boca contando 8 segundos"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.RESPIRACION)
                .titulo("Respiración cuadrada")
                .descripcion("4 tiempos iguales, ideal para bajar la ansiedad. 3 repeticiones.")
                .duracionSegundos(48)
                .ordenVisualizacion(2)
                .pasos(List.of(
                        "Repetición 1: Inhala contando 4 segundos", "Repetición 1: Sostén contando 4 segundos",
                        "Repetición 1: Exhala contando 4 segundos", "Repetición 1: Sostén contando 4 segundos",
                        "Repetición 2: Inhala contando 4 segundos", "Repetición 2: Sostén contando 4 segundos",
                        "Repetición 2: Exhala contando 4 segundos", "Repetición 2: Sostén contando 4 segundos",
                        "Repetición 3: Inhala contando 4 segundos", "Repetición 3: Sostén contando 4 segundos",
                        "Repetición 3: Exhala contando 4 segundos", "Repetición 3: Sostén contando 4 segundos"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.RESPIRACION)
                .titulo("Respiración diafragmática")
                .descripcion("Respiración profunda desde el vientre para relajar el cuerpo. 3 repeticiones.")
                .duracionSegundos(35)
                .ordenVisualizacion(3)
                .pasos(List.of(
                        "Coloca una mano en el pecho y otra en el vientre",
                        "Repetición 1: Inhala profundo por la nariz llenando el vientre (5s)",
                        "Repetición 1: Exhala lento por la boca vaciando el vientre (5s)",
                        "Repetición 2: Inhala profundo por la nariz llenando el vientre (5s)",
                        "Repetición 2: Exhala lento por la boca vaciando el vientre (5s)",
                        "Repetición 3: Inhala profundo por la nariz llenando el vientre (5s)",
                        "Repetición 3: Exhala lento por la boca vaciando el vientre (5s)"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.RESPIRACION)
                .titulo("Respiración 5-5 (coherente)")
                .descripcion("Respiración pareja para regular el ritmo cardíaco. 3 repeticiones.")
                .duracionSegundos(30)
                .ordenVisualizacion(4)
                .pasos(List.of(
                        "Repetición 1: Inhala contando 5 segundos", "Repetición 1: Exhala contando 5 segundos",
                        "Repetición 2: Inhala contando 5 segundos", "Repetición 2: Exhala contando 5 segundos",
                        "Repetición 3: Inhala contando 5 segundos", "Repetición 3: Exhala contando 5 segundos"
                ))
                .build());

        // ── MEDITACIÓN: más cortas para no generar ansiedad ──
        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.MEDITACION)
                .titulo("Meditación de 1 minuto")
                .descripcion("Una pausa exprés cuando no tienes mucho tiempo.")
                .duracionSegundos(60)
                .ordenVisualizacion(1)
                .pasos(List.of(
                        "Cierra los ojos y respira con calma",
                        "Nota las sensaciones de tu cuerpo sin juzgarlas",
                        "Abre los ojos lentamente cuando estés listo"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.MEDITACION)
                .titulo("Escaneo corporal exprés")
                .descripcion("Recorre tu cuerpo soltando tensión, sin apuro.")
                .duracionSegundos(90)
                .ordenVisualizacion(2)
                .pasos(List.of(
                        "Cierra los ojos y respira profundo",
                        "Lleva tu atención a los pies y relájalos",
                        "Sube la atención por piernas, torso y brazos, soltando tensión",
                        "Termina relajando el cuello y el rostro"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.MEDITACION)
                .titulo("Pausa de 3 minutos")
                .descripcion("Micro-meditación para retomar la concentración entre clases.")
                .duracionSegundos(150)
                .ordenVisualizacion(3)
                .pasos(List.of(
                        "Detente y nota cómo te sientes en este momento",
                        "Enfoca toda tu atención en tu respiración",
                        "Amplía la atención a todo tu cuerpo antes de continuar"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.MEDITACION)
                .titulo("Meditación para dormir mejor")
                .descripcion("Sesión guiada breve para relajar el cuerpo antes de dormir.")
                .duracionSegundos(150)
                .ordenVisualizacion(4)
                .pasos(List.of(
                        "Busca una posición cómoda y cierra los ojos",
                        "Lleva tu atención a la respiración, sin forzarla",
                        "Relaja los hombros, la mandíbula y las manos",
                        "Nota los sonidos a tu alrededor sin juzgarlos",
                        "Permite que tu cuerpo se sienta pesado y en calma"
                ))
                .build());

        // ── CRISIS: cortas y directas ──
        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.CRISIS)
                .titulo("Técnica de anclaje 5-4-3-2-1")
                .descripcion("Herramienta rápida para momentos de crisis o ansiedad intensa.")
                .duracionSegundos(100)
                .ordenVisualizacion(1)
                .pasos(List.of(
                        "Nombra 5 cosas que puedas ver a tu alrededor",
                        "Nombra 4 cosas que puedas tocar",
                        "Nombra 3 cosas que puedas escuchar",
                        "Nombra 2 cosas que puedas oler",
                        "Nombra 1 cosa que puedas saborear"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.CRISIS)
                .titulo("Técnica STOP")
                .descripcion("4 pasos para frenar un pico de ansiedad y recuperar el control.")
                .duracionSegundos(40)
                .ordenVisualizacion(2)
                .pasos(List.of(
                        "S - Detente: deja de hacer lo que estás haciendo",
                        "T - Toma aire: respira profundo una vez",
                        "O - Observa: qué sientes en tu cuerpo y a tu alrededor",
                        "P - Procede: elige con calma tu siguiente paso"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.CRISIS)
                .titulo("Frase de contención")
                .descripcion("Un mensaje corto para repetirte en momentos difíciles.")
                .duracionSegundos(25)
                .ordenVisualizacion(3)
                .pasos(List.of(
                        "Repite en voz baja: estoy a salvo, esto va a pasar",
                        "Aprieta y suelta los puños 3 veces mientras respiras",
                        "Si la crisis continúa, contacta a un psicólogo o a alguien de confianza ahora mismo"
                ))
                .build());

        recursoBienestarRepository.save(RecursoBienestar.builder()
                .tipo(TipoRecurso.CRISIS)
                .titulo("Mensaje de apoyo inmediato")
                .descripcion("Recordatorio breve para momentos de mucha angustia.")
                .duracionSegundos(30)
                .ordenVisualizacion(4)
                .pasos(List.of(
                        "Lo que sientes ahora es temporal, va a pasar",
                        "Respira: inhala 4 segundos, exhala 6 segundos",
                        "Si necesitas hablar con alguien, agenda una cita o contacta a tu línea de ayuda local"
                ))
                .build());
    }
}