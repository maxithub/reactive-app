package max.lab.r2app.reactiveapp;

import lombok.extern.slf4j.Slf4j;
import max.lab.r2app.reactiveapp.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class ReactiveAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveAppApplication.class, args);
	}

//	@Bean
	public CommandLineRunner commandLineRunner(AppUserRepository repository) {
		return (args -> repository.deleteAll().subscribe((v) -> log.info("Deleted all appusers")));
	}

}
