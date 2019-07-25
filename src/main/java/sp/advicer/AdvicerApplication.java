package sp.advicer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AdvicerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvicerApplication.class, args);
    }

}
