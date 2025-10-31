package com.projet.scraping.controller;

import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsersDebug() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("nom", user.getNom());
                    userInfo.put("roles", user.getRoles());
                    userInfo.put("enabled", user.isEnable());
                    userInfo.put("status", user.getStatut());
                    return userInfo;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/admin-check")
    public Map<String, Object> checkAdminUser() {
        Map<String, Object> result = new HashMap<>();
        
        boolean adminExists = userRepository.existsByEmail("admin@scraping.com");
        result.put("adminExists", adminExists);
        
        if (adminExists) {
            User admin = userRepository.findByEmail("admin@scraping.com").orElse(null);
            if (admin != null) {
                result.put("adminEmail", admin.getEmail());
                result.put("adminRoles", admin.getRoles());
                result.put("adminEnabled", admin.isEnable());
                result.put("adminStatus", admin.getStatut());
            }
        }
        
        result.put("totalUsers", userRepository.count());
        return result;
    }
}
