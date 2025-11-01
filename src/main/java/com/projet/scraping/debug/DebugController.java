package com.projet.scraping.debug;

import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("debugToolsController")
@RequestMapping("/api/v1/debug")
public class DebugController {

    private final UserRepository userRepository;

    public DebugController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "not_authenticated"));
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(Map.of(
                        "email", u.getEmail(),
                        "roles", String.valueOf(u.getRoles())
                )))
                .orElse(ResponseEntity.status(404).body(Map.of("error", "user_not_found")));
    }

    @PostMapping("/make-me-admin")
    public ResponseEntity<?> makeMeAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "not_authenticated"));
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> {
                    String roles = u.getRoles() == null ? "" : u.getRoles();
                    // Ensure ADMIN and USER roles are present
                    if (!roles.toUpperCase().contains("ADMIN")) {
                        roles = (roles.isBlank() ? "ADMIN" : roles + ",ADMIN");
                    }
                    if (!roles.toUpperCase().contains("USER")) {
                        roles = (roles.isBlank() ? "USER" : roles + ",USER");
                    }
                    u.setRoles(roles);
                    userRepository.save(u);
                    return ResponseEntity.ok(Map.of("status", "ok", "roles", roles));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "user_not_found")));
    }
}
