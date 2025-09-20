package com.finmate.finmate_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FinmateBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinmateBackendApplication.class, args);
	}

}
