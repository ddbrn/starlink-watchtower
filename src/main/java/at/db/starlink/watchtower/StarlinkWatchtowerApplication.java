package at.db.starlink.watchtower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StarlinkWatchtowerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarlinkWatchtowerApplication.class, args);
	}

}
