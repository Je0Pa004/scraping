package com.projet.scraping.config;

import com.projet.scraping.DtoRequest.TypeAbonnementRequest;
import com.projet.scraping.entities.enums.SubscriptionType;
import com.projet.scraping.security.model.User;
import com.projet.scraping.entities.enums.AccountType;
import com.projet.scraping.entities.enums.UserStatus;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.services.TypeAbonnementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final TypeAbonnementService typeAbonnementService;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            log.info("=== INITIALIZING DEFAULT DATA ===");

            // Initialize subscription types first
            initializeSubscriptionTypes();

            // Then initialize users
            initializeUsers(userRepository);
        };
    }

    private void initializeSubscriptionTypes() {
        log.info("=== INITIALIZING SUBSCRIPTION TYPES ===");

        try {
            // Check if Basic subscription exists
            if (typeAbonnementService.getAll().isEmpty()) {
                // Create Basic subscription
                TypeAbonnementRequest basicRequest = TypeAbonnementRequest.builder()
                    .nom("Abonnement Basic")
                    .description("Idéal pour les petites entreprises")
                    .cout(29.99)
                    .duree(30)
                    .nombreScrapingMax(100)
                    .nombreProfilsMax(50)
                    .type(SubscriptionType.MENSUEL)
                    .build();

                typeAbonnementService.save(basicRequest);
                log.info("Basic subscription type created");

                // Create Premium subscription
                TypeAbonnementRequest premiumRequest = TypeAbonnementRequest.builder()
                    .nom("Abonnement Premium")
                    .description("Pour les entreprises en croissance")
                    .cout(79.99)
                    .duree(30)
                    .nombreScrapingMax(500)
                    .nombreProfilsMax(200)
                    .type(SubscriptionType.MENSUEL)
                    .build();

                typeAbonnementService.save(premiumRequest);
                log.info("Premium subscription type created");

                // Create Enterprise subscription
                TypeAbonnementRequest enterpriseRequest = TypeAbonnementRequest.builder()
                    .nom("Abonnement Enterprise")
                    .description("Solution complète pour grandes entreprises")
                    .cout(199.99)
                    .duree(30)
                    .nombreScrapingMax(2000)
                    .nombreProfilsMax(1000)
                    .type(SubscriptionType.MENSUEL)
                    .build();

                typeAbonnementService.save(enterpriseRequest);
                log.info("Enterprise subscription type created");
            }
        } catch (Exception e) {
            log.error("Error initializing subscription types: ", e);
        }
    }

    private void initializeUsers(UserRepository userRepository) {
        log.info("=== INITIALIZING DEFAULT USERS ===");

        // Upsert admin par défaut (garantit email/mot de passe attendus)
        userRepository.findByEmail("admin@gmail.com").ifPresentOrElse(admin -> {
            admin.setNom("Administrateur");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRoles("ROLE_ADMIN,ROLE_USER");
            admin.setEnable(true);
            admin.setStatut(UserStatus.ACTIVE);
            admin.setTypeCompte(AccountType.ADMIN);
            userRepository.save(admin);
            log.info("Admin user updated: admin@gmail.com / password");
        }, () -> {
            User admin = new User();
            admin.setNom("Administrateur");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRoles("ROLE_ADMIN,ROLE_USER");
            admin.setEnable(true);
            admin.setStatut(UserStatus.ACTIVE);
            admin.setTypeCompte(AccountType.ADMIN);
            userRepository.save(admin);
            log.info("Admin user created: admin@gmail.com / password");
        });

        // Créer un utilisateur de test
        if (!userRepository.existsByEmail("user@test.com")) {
            User user = new User();
            user.setNom("Utilisateur Test");
            user.setEmail("user@test.com");
            user.setPassword(passwordEncoder.encode("User123!"));
            user.setRoles("ROLE_USER");
            user.setEnable(true);
            user.setStatut(UserStatus.ACTIVE);
            user.setTypeCompte(AccountType.RECRUTEUR);

            userRepository.save(user);
            log.info("Test user created: user@test.com / User123!");
        }

        // Créer un utilisateur premium de test
        if (!userRepository.existsByEmail("premium@test.com")) {
            User premiumUser = new User();
            premiumUser.setNom("Utilisateur Premium");
            premiumUser.setEmail("premium@test.com");
            premiumUser.setPassword(passwordEncoder.encode("Premium123!"));
            premiumUser.setRoles("ROLE_USER,ROLE_PREMIUM");
            premiumUser.setEnable(true);
            premiumUser.setStatut(UserStatus.ACTIVE);
            premiumUser.setTypeCompte(AccountType.ENTREPRISE);

            userRepository.save(premiumUser);
            log.info("Premium user created: premium@test.com / Premium123!");
        }
    }
}
