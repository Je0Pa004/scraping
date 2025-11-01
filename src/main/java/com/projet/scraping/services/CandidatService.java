package com.projet.scraping.services;

import com.projet.scraping.DtoRequest.InvitationRequest;
import com.projet.scraping.entities.Candidat;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidatService {
    Candidat inviter(InvitationRequest req);
    List<Candidat> getAll();
    Candidat uploadCv(String token, MultipartFile file);
}
