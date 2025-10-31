package com.projet.scraping.admin;

import com.projet.scraping.admin.dto.*;
import com.projet.scraping.Exeption.ResourceNotFoundException;
import com.projet.scraping.entities.Abonnement;
import com.projet.scraping.repositories.AbonnementRepository;
import com.projet.scraping.repositories.PaiementRepository;
import com.projet.scraping.repositories.ScrapingRepository;
import com.projet.scraping.security.model.History;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.services.AbonnementService;
import com.projet.scraping.entities.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ScrapingRepository scrapingRepository;
    private final PaiementRepository paiementRepository;
    private final com.projet.scraping.security.repository.HistoryRepository historyRepository;
    private final AbonnementRepository abonnementRepository;
    private final AbonnementService abonnementService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminStatsDTO getAdminStats() {
        long totalUsers = userRepository.count();
        long totalSearches = scrapingRepository.count();
        
        // Count profiles from all scraping results
        long totalProfiles = scrapingRepository.findAll().stream()
                .filter(s -> s.getNombreProfilScrape() != null)
                .mapToLong(scraping -> scraping.getNombreProfilScrape())
                .sum();

        List<User> allUsers = userRepository.findAll();
        long freeUsers = allUsers.stream()
                .filter(u -> u.getRoles() != null && !u.getRoles().contains("PREMIUM") && !u.getRoles().contains("BASIC"))
                .count();
        long basicUsers = allUsers.stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("BASIC"))
                .count();
        long premiumUsers = allUsers.stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains("PREMIUM"))
                .count();

        double monthlyRevenue = paiementRepository.findAll().stream()
                .filter(p -> p.getDatePaiement() != null && p.getDatePaiement().isAfter(java.time.LocalDate.now().minusMonths(1)))
                .filter(p -> p.getStatut() != null && p.getStatut() == com.projet.scraping.entities.enums.PaymentStatus.SUCCESS)
                .filter(p -> p.getMontant() != null)
                .mapToDouble(com.projet.scraping.entities.Paiement::getMontant)
                .sum();

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalSearches(totalSearches)
                .totalProfiles(totalProfiles)
                .monthlyRevenue(monthlyRevenue)
                .freeUsers(freeUsers)
                .basicUsers(basicUsers)
                .premiumUsers(premiumUsers)
                .build();
    }

    public List<PaymentDTO> getAllPayments() {
        return paiementRepository.findAllByOrderByDatePaiementDesc().stream()
                .filter(payment -> payment.getUtilisateur() != null)
                .map(payment -> {
                    PaymentDTO.PaymentStatus mappedStatus = PaymentDTO.PaymentStatus.PENDING;
                    if (payment.getStatut() != null) {
                        switch (payment.getStatut()) {
                            case SUCCESS -> mappedStatus = PaymentDTO.PaymentStatus.SUCCEEDED;
                            case PENDING -> mappedStatus = PaymentDTO.PaymentStatus.PENDING;
                            case FAILED -> mappedStatus = PaymentDTO.PaymentStatus.FAILED;
                        }
                    }
                    return PaymentDTO.builder()
                            .id(payment.getId().toString())
                            .transactionId(payment.getPublicId() != null ? payment.getPublicId().toString() : "N/A")
                            .user(payment.getUtilisateur().getNom() != null ? payment.getUtilisateur().getNom() : "Unknown")
                            .email(payment.getUtilisateur().getEmail() != null ? payment.getUtilisateur().getEmail() : "N/A")
                            .plan(payment.getUtilisateur().getRoles() != null && payment.getUtilisateur().getRoles().contains("PREMIUM") ? "Premium" :
                                    payment.getUtilisateur().getRoles() != null && payment.getUtilisateur().getRoles().contains("BASIC") ? "Basic" : "Free")
                            .amount(payment.getMontant() != null ? payment.getMontant() : 0.0)
                            .date(payment.getDatePaiement() != null ? payment.getDatePaiement().atStartOfDay() : LocalDateTime.now())
                            .status(mappedStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO updatePaymentStatusAdmin(Long paiementId, com.projet.scraping.entities.enums.PaymentStatus status) {
        var paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new com.projet.scraping.Exeption.ResourceNotFoundException("Paiement not found with id: " + paiementId));

        paiement.setStatut(status);
        paiementRepository.save(paiement);

        if (status == com.projet.scraping.entities.enums.PaymentStatus.SUCCESS && paiement.getAbonnement() != null) {
            var abonnement = paiement.getAbonnement();
            abonnement.setStatut(true);
            abonnementRepository.save(abonnement);
        }

        PaymentDTO.PaymentStatus mappedStatus = PaymentDTO.PaymentStatus.PENDING;
        if (paiement.getStatut() != null) {
            switch (paiement.getStatut()) {
                case SUCCESS -> mappedStatus = PaymentDTO.PaymentStatus.SUCCEEDED;
                case PENDING -> mappedStatus = PaymentDTO.PaymentStatus.PENDING;
                case FAILED -> mappedStatus = PaymentDTO.PaymentStatus.FAILED;
            }
        }

        return PaymentDTO.builder()
                .id(paiement.getId().toString())
                .transactionId(paiement.getPublicId() != null ? paiement.getPublicId().toString() : "N/A")
                .user(paiement.getUtilisateur() != null && paiement.getUtilisateur().getNom() != null ? paiement.getUtilisateur().getNom() : "Unknown")
                .email(paiement.getUtilisateur() != null && paiement.getUtilisateur().getEmail() != null ? paiement.getUtilisateur().getEmail() : "N/A")
                .plan(paiement.getUtilisateur() != null && paiement.getUtilisateur().getRoles() != null && paiement.getUtilisateur().getRoles().contains("PREMIUM") ? "Premium" :
                        paiement.getUtilisateur() != null && paiement.getUtilisateur().getRoles() != null && paiement.getUtilisateur().getRoles().contains("BASIC") ? "Basic" : "Free")
                .amount(paiement.getMontant() != null ? paiement.getMontant() : 0.0)
                .date(paiement.getDatePaiement() != null ? paiement.getDatePaiement().atStartOfDay() : LocalDateTime.now())
                .status(mappedStatus)
                .build();
    }

    @Transactional
    public void refundPayment(String paymentId) {
        // Mock implementation - to be replaced with real payment refund logic
        // This would typically call a payment gateway API
    }

    public List<ScrapingActivityDTO> getAllScrapingActivities() {
        return scrapingRepository.findAll().stream()
                .map(scraping -> {
                    User user = scraping.getUtilisateur();
                    return ScrapingActivityDTO.builder()
                            .id(scraping.getPublicId().toString())
                            .user(user != null ? user.getNom() : "Unknown")
                            .email(user != null ? user.getEmail() : "N/A")
                            .criteria(scraping.getTitre() + " - " + scraping.getSecteur())
                            .location(scraping.getLocalisation())
                            .source(scraping.getSource().name())
                            .date(scraping.getDateDemande() != null ? scraping.getDateDemande().atStartOfDay() : LocalDateTime.now())
                            .profilesFound(scraping.getNombreProfilScrape())
                            .status(mapScrapingStatus(scraping.getStatut()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ScrapingActivityDTO.ScrapingStatus mapScrapingStatus(com.projet.scraping.entities.enums.ScrapingStatus statut) {
        if (statut == null) return ScrapingActivityDTO.ScrapingStatus.IN_PROGRESS;
        // Map possible French-named enum values to DTO enum
        try {
            return switch (statut.name()) {
                case "EN_COURS" -> ScrapingActivityDTO.ScrapingStatus.IN_PROGRESS;
                case "TERMINE" -> ScrapingActivityDTO.ScrapingStatus.COMPLETED;
                case "ECHEC" -> ScrapingActivityDTO.ScrapingStatus.FAILED;
                default -> ScrapingActivityDTO.ScrapingStatus.IN_PROGRESS;
            };
        } catch (Exception e) {
            return ScrapingActivityDTO.ScrapingStatus.IN_PROGRESS;
        }
    }

    @Transactional
    public void stopScraping(String scrapingId) {
        // Mock implementation - to be replaced with real scraping stop logic
        // This would typically stop an ongoing scraping process
    }

    public List<SystemLogDTO> getSystemLogs() {
        return historyRepository.findAllByOrderByDateHistoryDesc().stream()
                .filter(history -> history != null && history.getDateHistory() != null)
                .map(history -> SystemLogDTO.builder()
                        .id(history.getId() != null ? history.getId().toString() : "0")
                        .timestamp(history.getDateHistory().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .level(SystemLogDTO.LogLevel.INFO)
                        .source(history.getUser() != null && history.getUser().getNom() != null ? history.getUser().getNom() : "system")
                        .message(history.getName() != null ? history.getName() : "No message")
                        .user(history.getUser() != null && history.getUser().getEmail() != null ? history.getUser().getEmail() : "system")
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void suspendUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            user.setEnable(false);
            user.setStatut(UserStatus.SUSPENDED);
            userRepository.save(user);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + userId);
        }
    }

    @Transactional
    public void activateUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            user.setEnable(true);
            user.setStatut(UserStatus.ACTIVE);
            userRepository.save(user);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + userId);
        }
    }

    public List<UserManagementDTO> getAllUsersForAdmin() {
        try {
            List<User> users = userRepository.findAll();
            return users.stream()
                    .filter(user -> user != null)
                    .map(user -> {
                        try {
                            return convertToUserManagementDTO(user);
                        } catch (Exception e) {
                            // Log et ignore l'utilisateur en cas d'erreur
                            System.err.println("Erreur conversion user " + user.getId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur getAllUsersForAdmin: " + e.getMessage());
            e.printStackTrace();
            // Retourne une liste vide en cas d'erreur totale
            return Collections.emptyList();
        }
    }

    public UserManagementDTO getUserDetails(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return convertToUserManagementDTO(user);
    }

    @Transactional
    public UserManagementDTO updateUser(String userId, UserManagementDTO userDTO) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Update user fields
        if (userDTO.getFullName() != null) {
            user.setNom(userDTO.getFullName());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getStatus() != null) {
            user.setStatut(UserStatus.valueOf(userDTO.getStatus()));
            user.setEnable("ACTIVE".equals(userDTO.getStatus()));
        }
        
        User savedUser = userRepository.save(user);
        return convertToUserManagementDTO(savedUser);
    }

    @Transactional
    public void deleteUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            if (!userRepository.existsById(id)) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            userRepository.deleteById(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + userId);
        }
    }

    @Transactional
    public UserManagementDTO createUser(CreateUserDTO createUserDTO) {
        // Check if email already exists
        if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + createUserDTO.getEmail());
        }

        // Enforce single ADMIN account
        if (createUserDTO.getRoles() != null && createUserDTO.getRoles().equalsIgnoreCase("ADMIN")) {
            boolean adminExists = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRoles() != null && u.getRoles().toUpperCase().contains("ROLE_ADMIN"));
            if (adminExists) {
                throw new IllegalStateException("Un compte Super Admin existe déjà. Impossible d'en créer un autre.");
            }
        }

        // Create new user
        User newUser = new User();
        newUser.setNom(createUserDTO.getFullName());
        newUser.setEmail(createUserDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        newUser.setRoles(createUserDTO.getRoles() != null ? "ROLE_" + createUserDTO.getRoles() : "ROLE_USER");
        newUser.setStatut(createUserDTO.getStatus() != null ? UserStatus.valueOf(createUserDTO.getStatus()) : UserStatus.ACTIVE);
        newUser.setEnable(true);
        newUser.setCreateDate(Instant.now());
        newUser.setDateM(Instant.now());
        
        // Set AccountType based on roles
        if (createUserDTO.getRoles() != null) {
            try {
                newUser.setTypeCompte(com.projet.scraping.entities.enums.AccountType.valueOf(createUserDTO.getRoles()));
            } catch (IllegalArgumentException e) {
                // Default to USER if role doesn't match AccountType enum
                newUser.setTypeCompte(com.projet.scraping.entities.enums.AccountType.USER);
            }
        } else {
            newUser.setTypeCompte(com.projet.scraping.entities.enums.AccountType.USER);
        }

        User savedUser = userRepository.save(newUser);
        return convertToUserManagementDTO(savedUser);
    }

    private UserManagementDTO convertToUserManagementDTO(User user) {
        try {
            // Récupérer l'abonnement actif de l'utilisateur
            Abonnement activeAbonnement = null;
            try {
                activeAbonnement = abonnementRepository.findTopByUtilisateur_IdOrderByDateDebutDesc(user.getId())
                        .orElse(null);
            } catch (Exception e) {
                // Ignore si erreur de récupération d'abonnement
            }
            
            // Calculer le nombre de profils scrapés
            long totalProfilesScraped = 0;
            try {
                totalProfilesScraped = scrapingRepository.findAll().stream()
                        .filter(s -> s.getUtilisateur() != null && s.getUtilisateur().getId() != null)
                        .filter(s -> s.getUtilisateur().getId().equals(user.getId()))
                        .mapToLong(s -> s.getNombreProfilScrape() != null ? s.getNombreProfilScrape() : 0)
                        .sum();
            } catch (Exception e) {
                // Ignore si erreur de calcul
            }
            
            // Déterminer le plan et le quota basé sur l'abonnement actif
            String plan = "Aucun";
            int searchQuota = 0;
            int searchesUsed = 0;
            
            if (activeAbonnement != null && activeAbonnement.getStatut() != null && activeAbonnement.getStatut() && 
                activeAbonnement.getDateFin() != null && activeAbonnement.getDateFin().isAfter(LocalDate.now()) &&
                activeAbonnement.getTypeAbonnement() != null) {
                plan = activeAbonnement.getTypeAbonnement().getNom();
                searchQuota = activeAbonnement.getTypeAbonnement().getNombreScrapingMax();
                searchesUsed = (int) totalProfilesScraped;
            }
            
            double quotaPercentage = (searchQuota > 0) ? (searchesUsed * 100.0 / searchQuota) : 0;
            
            // Extraire les rôles
            String userRoles = "Utilisateur";
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                userRoles = user.getRoles().replace("ROLE_", "").replace(",", ", ");
            }
            
            // Calculer le solde total payé par l'utilisateur
            double totalBalance = 0;
            try {
                totalBalance = paiementRepository.findAll().stream()
                        .filter(p -> p.getUtilisateur() != null && p.getUtilisateur().getId() != null)
                        .filter(p -> p.getUtilisateur().getId().equals(user.getId()))
                        .filter(p -> p.getStatut() != null && p.getStatut() == com.projet.scraping.entities.enums.PaymentStatus.SUCCESS)
                        .filter(p -> p.getMontant() != null)
                        .mapToDouble(p -> p.getMontant())
                        .sum();
            } catch (Exception e) {
                // Ignore si erreur de calcul
            }
            
            return UserManagementDTO.builder()
                    .id(user.getId() != null ? user.getId().toString() : "0")
                    .fullName(user.getNom() != null ? user.getNom() : "Unknown")
                    .email(user.getEmail() != null ? user.getEmail() : "no-email@example.com")
                    .roles(userRoles)
                    .plan(plan)
                    .registrationDate(user.getCreateDate() != null ? 
                        LocalDateTime.ofInstant(user.getCreateDate(), java.time.ZoneId.systemDefault()) : 
                        LocalDateTime.now())
                    .searchesUsed(searchesUsed)
                    .searchQuota(searchQuota)
                    .quotaPercentage(quotaPercentage)
                    .status(user.getStatut() != null ? user.getStatut().name() : "ACTIVE")
                    .phone("") // Phone field not in User entity
                    .company("") // Company field not in User entity
                    .emailVerified(true) // Assuming email is verified if user is active
                    .lastLogin(user.getDateM() != null ?
                        LocalDateTime.ofInstant(user.getDateM(), java.time.ZoneId.systemDefault()) :
                        null)
                    .build();
        } catch (Exception e) {
            // En cas d'erreur totale, retourner un DTO minimal
            return UserManagementDTO.builder()
                    .id(user.getId() != null ? user.getId().toString() : "0")
                    .fullName(user.getNom() != null ? user.getNom() : "Unknown")
                    .email(user.getEmail() != null ? user.getEmail() : "no-email@example.com")
                    .roles("Utilisateur")
                    .plan("Aucun")
                    .registrationDate(LocalDateTime.now())
                    .searchesUsed(0)
                    .searchQuota(0)
                    .quotaPercentage(0)
                    .status("ACTIVE")
                    .phone("")
                    .company("")
                    .emailVerified(true)
                    .lastLogin(null)
                    .build();
        }
    }
}
