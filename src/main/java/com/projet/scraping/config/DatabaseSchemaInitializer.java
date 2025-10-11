package com.projet.scraping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseSchemaInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeDatabase() {
        try {
            // Add the missing mot_de_passe column to users table
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN mot_de_passe VARCHAR(255) NOT NULL DEFAULT 'temp_password'");
            System.out.println("✅ Database schema initialized successfully - mot_de_passe column added to users table");
        } catch (Exception e) {
            // Column might already exist, which is fine
            System.out.println("ℹ️ Database schema already up to date or column already exists: " + e.getMessage());
        }

        try {
            // Update existing records with null public_id for all BaseEntity subclasses
            String[] tables = {"type_abonnement", "abonnement", "history", "paiement", "profil_scraper", "scraping", "roles"};
            for (String table : tables) {
                int updatedCount = jdbcTemplate.update(
                    "UPDATE " + table + " SET public_id = gen_random_uuid() WHERE public_id IS NULL"
                );
                if (updatedCount > 0) {
                    System.out.println("✅ Updated " + updatedCount + " " + table + " records with missing public_id");
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Could not update entity public_ids (might not be needed): " + e.getMessage());
        }
    }
}
