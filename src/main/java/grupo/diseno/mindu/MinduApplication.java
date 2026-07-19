package grupo.diseno.mindu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MinduApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinduApplication.class, args);
    }
}