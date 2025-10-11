package com.projet.scraping.controller;

import com.projet.scraping.DtoRequest.TypeAbonnementRequest;
import com.projet.scraping.DtoResponse.TypeAbonnementResponse;
import com.projet.scraping.services.TypeAbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/type-abonnements")
@RequiredArgsConstructor
public class TypeAbonnementController {

    private final TypeAbonnementService typeAbonnementService;

    @PostMapping
    public ResponseEntity<TypeAbonnementResponse> save(@RequestBody TypeAbonnementRequest request) {
        return ResponseEntity.ok(typeAbonnementService.save(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeAbonnementResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(typeAbonnementService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<TypeAbonnementResponse>> getAll() {
        return ResponseEntity.ok(typeAbonnementService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeAbonnementResponse> update(@PathVariable Long id, @RequestBody TypeAbonnementRequest request) {
        return ResponseEntity.ok(typeAbonnementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        typeAbonnementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
