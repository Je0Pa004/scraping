package com.projet.scraping.controller;

/**
 * Contrôleur REST pour les opérations de scraping.
 * Ce contrôleur expose les endpoints pour gérer les scrapings, y compris
 * la création, la récupération, la mise à jour et la suppression.
 */
import com.projet.scraping.DtoRequest.ScrapingRequest;
import com.projet.scraping.DtoResponse.ScrapingResponse;
import com.projet.scraping.services.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scrapings")
@RequiredArgsConstructor
public class ScrapingController {

    private final ScrapingService scrapingService;

    @PostMapping
    public ResponseEntity<ScrapingResponse> save(@RequestBody ScrapingRequest request) {
        return ResponseEntity.ok(scrapingService.save(request));
    }

    @PostMapping("/perform")
    public ResponseEntity<ScrapingResponse> perform(@RequestBody ScrapingRequest request) {
        return ResponseEntity.ok(scrapingService.performScraping(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScrapingResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(scrapingService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ScrapingResponse>> getAll() {
        return ResponseEntity.ok(scrapingService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScrapingResponse> update(@PathVariable Long id, @RequestBody ScrapingRequest request) {
        return ResponseEntity.ok(scrapingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scrapingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/public/{publicId}")
    public ResponseEntity<Void> deleteByPublic(@PathVariable UUID publicId) {
        scrapingService.deleteByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }
}
