package MyBot.gymbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class GymbotApplication {
	public static void main(String[] args) {
		SpringApplication.run(GymbotApplication.class, args);
	}
}
