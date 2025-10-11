package com.projet.scraping.controller;

import com.projet.scraping.DtoRequest.ProfilScraperRequest;
import com.projet.scraping.DtoResponse.ProfilScraperResponse;
import com.projet.scraping.services.ProfilScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profil-scrapers")
@RequiredArgsConstructor
public class ProfilScraperController {

    private final ProfilScraperService profilScraperService;

    @PostMapping
    public ResponseEntity<ProfilScraperResponse> save(@RequestBody ProfilScraperRequest request) {
        return ResponseEntity.ok(profilScraperService.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfilScraperResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(profilScraperService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ProfilScraperResponse>> getAll() {
        return ResponseEntity.ok(profilScraperService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfilScraperResponse> update(@PathVariable Long id, @RequestBody ProfilScraperRequest request) {
        return ResponseEntity.ok(profilScraperService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profilScraperService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
