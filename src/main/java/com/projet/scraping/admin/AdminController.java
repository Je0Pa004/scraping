package com.projet.scraping.admin;

import com.projet.scraping.admin.dto.*;
import com.projet.scraping.DtoRequest.TypeAbonnementRequest;
import com.projet.scraping.DtoResponse.TypeAbonnementResponse;
import com.projet.scraping.services.TypeAbonnementService;
import jakarta.validation.Valid;
import com.projet.scraping.security.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administration", description = "Endpoints pour l'administration de la plateforme")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final TypeAbonnementService typeAbonnementService;

    public AdminController(AdminService adminService, UserRepository userRepository, TypeAbonnementService typeAbonnementService) {
        this.adminService = adminService;
        this.userRepository = userRepository;
        this.typeAbonnementService = typeAbonnementService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Récupérer les statistiques du dashboard admin")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    @GetMapping("/payments")
    @Operation(summary = "Récupérer tous les paiements")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @PostMapping("/payments/{paymentId}/refund")
    @Operation(summary = "Rembourser un paiement")
    public ResponseEntity<Void> refundPayment(@PathVariable String paymentId) {
        adminService.refundPayment(paymentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/payments/{paymentId}/status")
    @Operation(summary = "Mettre à jour le statut d'un paiement (admin)")
    public ResponseEntity<com.projet.scraping.admin.dto.PaymentDTO> updatePaymentStatusAdmin(
            @PathVariable Long paymentId,
            @RequestBody com.projet.scraping.DtoRequest.PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(adminService.updatePaymentStatusAdmin(paymentId, request.getStatus()));
    }

    @GetMapping("/scraping")
    @Operation(summary = "Récupérer toutes les activités de scraping")
    public ResponseEntity<List<ScrapingActivityDTO>> getAllScrapingActivities() {
        return ResponseEntity.ok(adminService.getAllScrapingActivities());
    }

    @PostMapping("/scraping/{scrapingId}/stop")
    @Operation(summary = "Arrêter une recherche de scraping")
    public ResponseEntity<Void> stopScraping(@PathVariable String scrapingId) {
        adminService.stopScraping(scrapingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs")
    @Operation(summary = "Récupérer les logs système")
    public ResponseEntity<List<SystemLogDTO>> getSystemLogs() {
        return ResponseEntity.ok(adminService.getSystemLogs());
    }

    @PutMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspendre un utilisateur")
    public ResponseEntity<Void> suspendUser(@PathVariable String userId) {
        adminService.suspendUser(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/activate")
    @Operation(summary = "Activer un utilisateur")
    public ResponseEntity<Void> activateUser(@PathVariable String userId) {
        adminService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    @Operation(summary = "Récupérer tous les utilisateurs pour l'admin")
    public ResponseEntity<List<UserManagementDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsersForAdmin());
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Récupérer les détails d'un utilisateur")
    public ResponseEntity<UserManagementDTO> getUserDetails(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Modifier un utilisateur")
    public ResponseEntity<UserManagementDTO> updateUser(@PathVariable String userId, @RequestBody UserManagementDTO userDTO) {
        return ResponseEntity.ok(adminService.updateUser(userId, userDTO));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    @Operation(summary = "Créer un nouvel utilisateur")
    public ResponseEntity<UserManagementDTO> createUser(@RequestBody CreateUserDTO createUserDTO) {
        return ResponseEntity.ok(adminService.createUser(createUserDTO));
    }

    // ===== Gestion des Types d'Abonnements =====
    
    @PostMapping("/subscriptions/types")
    @Operation(summary = "Créer un nouveau type d'abonnement")
    public ResponseEntity<TypeAbonnementResponse> createSubscriptionType(@Valid @RequestBody TypeAbonnementRequest request) {
        return ResponseEntity.ok(typeAbonnementService.save(request));
    }

    @GetMapping("/subscriptions/types")
    @Operation(summary = "Récupérer tous les types d'abonnements")
    public ResponseEntity<List<TypeAbonnementResponse>> getAllSubscriptionTypes() {
        return ResponseEntity.ok(typeAbonnementService.getAll());
    }

    @GetMapping("/subscriptions/types/{id}")
    @Operation(summary = "Récupérer un type d'abonnement par ID")
    public ResponseEntity<TypeAbonnementResponse> getSubscriptionType(@PathVariable Long id) {
        return ResponseEntity.ok(typeAbonnementService.get(id));
    }

    @PutMapping("/subscriptions/types/{id}")
    @Operation(summary = "Modifier un type d'abonnement")
    public ResponseEntity<TypeAbonnementResponse> updateSubscriptionType(@PathVariable Long id, @Valid @RequestBody TypeAbonnementRequest request) {
        return ResponseEntity.ok(typeAbonnementService.update(id, request));
    }

    @DeleteMapping("/subscriptions/types/{id}")
    @Operation(summary = "Supprimer un type d'abonnement")
    public ResponseEntity<Void> deleteSubscriptionType(@PathVariable Long id) {
        typeAbonnementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
