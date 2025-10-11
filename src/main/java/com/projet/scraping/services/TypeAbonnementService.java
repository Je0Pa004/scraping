package com.projet.scraping.services;

import com.projet.scraping.DtoRequest.TypeAbonnementRequest;
import com.projet.scraping.DtoResponse.TypeAbonnementResponse;

import java.util.List;

public interface TypeAbonnementService {
    // CRUD
    TypeAbonnementResponse save(TypeAbonnementRequest req);
    TypeAbonnementResponse get(Long id);
    List<TypeAbonnementResponse> getAll();
    TypeAbonnementResponse update(Long id, TypeAbonnementRequest req);
    void delete(Long id);
}
