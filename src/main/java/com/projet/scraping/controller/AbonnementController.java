package com.projet.scraping.controller;

/**
 * Contrôleur REST pour les opérations sur les abonnements.
 * Ce contrôleur expose les endpoints pour gérer les abonnements des utilisateurs,
 * y compris la création, la récupération, la mise à jour et la suppression.
 */
import com.projet.scraping.DtoRequest.AbonnementRequest;
import com.projet.scraping.DtoResponse.AbonnementResponse;
import com.projet.scraping.services.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService abonnementService;

    @PostMapping
    public ResponseEntity<AbonnementResponse> save(@RequestBody AbonnementRequest request) {
        return ResponseEntity.ok(abonnementService.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbonnementResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(abonnementService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<AbonnementResponse>> getAll() {
        return ResponseEntity.ok(abonnementService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AbonnementResponse> update(@PathVariable Long id, @RequestBody AbonnementRequest request) {
        return ResponseEntity.ok(abonnementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        abonnementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
