package com.projet.scraping.services.implementation;

import com.projet.scraping.DtoRequest.TypeAbonnementRequest;
import com.projet.scraping.DtoResponse.TypeAbonnementResponse;
import com.projet.scraping.Mappers.TypeAbonnementMapper;
import com.projet.scraping.entities.TypeAbonnement;
import com.projet.scraping.repositories.TypeAbonnementRepository;
import com.projet.scraping.services.TypeAbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeAbonnementServiceImpl implements TypeAbonnementService {

    private final TypeAbonnementRepository typeAbonnementRepository;
    private final TypeAbonnementMapper typeAbonnementMapper;

    @Override
    public TypeAbonnementResponse save(TypeAbonnementRequest req) {
        TypeAbonnement e = typeAbonnementMapper.toEntity(req);
        e = typeAbonnementRepository.save(e);
        return typeAbonnementMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public TypeAbonnementResponse get(Long id) {
        TypeAbonnement e = typeAbonnementRepository.findById(id).orElse(null);
        return e != null ? typeAbonnementMapper.toResponse(e) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeAbonnementResponse> getAll() {
        return typeAbonnementRepository.findAll().stream().map(typeAbonnementMapper::toResponse).toList();
    }

    @Override
    public TypeAbonnementResponse update(Long id, TypeAbonnementRequest req) {
        TypeAbonnement e = typeAbonnementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement introuvable: " + id));
        typeAbonnementMapper.update(e, req);
        e = typeAbonnementRepository.save(e);
        return typeAbonnementMapper.toResponse(e);
    }

    @Override
    public void delete(Long id) {
        typeAbonnementRepository.deleteById(id);
    }

    @Override
    public TypeAbonnementResponse toggleStatus(Long id) {
        TypeAbonnement e = typeAbonnementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeAbonnement introuvable: " + id));
        e.setEstActif(!e.getEstActif());
        e = typeAbonnementRepository.save(e);
        return typeAbonnementMapper.toResponse(e);
    }
}
