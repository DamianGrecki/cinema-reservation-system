package pl.dgrecki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CinemaServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(CinemaServiceApplication.class, args);
    }
}
