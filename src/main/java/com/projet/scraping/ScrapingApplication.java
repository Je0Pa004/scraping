package com.projet.scraping;

import com.projet.scraping.security.enums.UserRole;
import com.projet.scraping.security.repository.RoleRepository;
import com.projet.scraping.security.model.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ScrapingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrapingApplication.class, args);
	}

	@Bean
	public CommandLineRunner start(RoleRepository roleRepository){
		return args -> {
			if (roleRepository.count() == 0 ){
				Role admin = new Role();
				admin.setName(UserRole.ADMIN);
				roleRepository.save(admin);

				Role recruteur = new Role();
				recruteur.setName(UserRole.RECRUTEUR);
				roleRepository.save(recruteur);

				Role entreprise = new Role();
				entreprise.setName(UserRole.ENTREPRISE);
				roleRepository.save(entreprise);
			}
		};
	}
}
