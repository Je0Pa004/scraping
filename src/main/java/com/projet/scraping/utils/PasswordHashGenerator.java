package com.projet.scraping.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitaire pour générer des hashs BCrypt pour les mots de passe
 * Utilisez cette classe pour générer les hashs à insérer dans la base de données
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Générer les hashs pour les mots de passe de test
        String[] passwords = {
            "Admin123!",
            "User123!",
            "Premium123!",
            "Test123!"
        };
        
        System.out.println("=== Hashs BCrypt pour les mots de passe ===\n");
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Mot de passe: " + password);
            System.out.println("Hash BCrypt: " + hash);
            System.out.println("---");
        }
        
        // Vérification
        System.out.println("\n=== Vérification ===");
        String testPassword = "Admin123!";
        String testHash = encoder.encode(testPassword);
        boolean matches = encoder.matches(testPassword, testHash);
        System.out.println("Le mot de passe '" + testPassword + "' correspond au hash: " + matches);
    }
}
