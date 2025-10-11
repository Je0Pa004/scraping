package com.projet.scraping.controller;

import com.projet.scraping.DtoRequest.PaiementCrudRequest;
import com.projet.scraping.DtoResponse.PaiementResponse;
import com.projet.scraping.services.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping
    public ResponseEntity<PaiementResponse> save(@RequestBody PaiementCrudRequest request) {
        return ResponseEntity.ok(paiementService.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paiementService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<PaiementResponse>> getAll() {
        return ResponseEntity.ok(paiementService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaiementResponse> update(@PathVariable Long id, @RequestBody PaiementCrudRequest request) {
        return ResponseEntity.ok(paiementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paiementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
