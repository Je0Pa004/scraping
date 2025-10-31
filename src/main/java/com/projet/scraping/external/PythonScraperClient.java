package com.projet.scraping.external;

import com.projet.scraping.DtoRequest.ScrapingRequest;
import com.projet.scraping.entities.enums.ApiSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class PythonScraperClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = System.getenv().getOrDefault("PYTHON_SCRAPER_URL", "http://localhost:5000/scrape");

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> scrape(ScrapingRequest req, Integer maxResults) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("source", mapSource(req.getSource()));
        payload.put("titre", nullToEmpty(req.getTitre()));
        payload.put("secteur", nullToEmpty(req.getSecteur()));
        payload.put("localisation", nullToEmpty(req.getLocalisation()));
        payload.put("entreprise", nullToEmpty(req.getEntreprise()));
        payload.put("emploi", nullToEmpty(req.getEmploi()));
        payload.put("taille_entreprise", nullToEmpty(req.getTailleEntreprise()));
        payload.put("keywords", nullToEmpty(req.getTitre()));
        payload.put("location", nullToEmpty(req.getLocalisation()));
        payload.put("profession", nullToEmpty(req.getEmploi()));
        payload.put("max_results", maxResults != null ? maxResults : 50);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                Map<String, Object> resp = restTemplate.postForObject(baseUrl, entity, Map.class);
                if (resp == null) return Collections.emptyList();
                Object arr = resp.get("results");
                if (arr instanceof List) {
                    return (List<Map<String, Object>>) arr;
                }
                return Collections.emptyList();
            } catch (RestClientException ex) {
                try {
                    Thread.sleep(500L * attempts);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return Collections.emptyList();
    }

    private static String mapSource(ApiSource src) {
        if (src == null) return "linkedin";
        return switch (src) {
            case LINKEDIN -> "linkedin";
            case PAGE_JAUNE -> "pagesjaunes";
            case GOOGLE_MAP -> "google_maps";
        };
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
