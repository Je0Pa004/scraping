package com.projet.scraping.security.jwt;


import com.projet.scraping.security.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre qui intercepte chaque requête pour vérifier la présence et la validité du token JWT.
 * Si le token est valide, il authentifie l'utilisateur auprès de Spring Security.
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtUtils jwtUtils;

    private final UserService userService;

    public AuthTokenFilter(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    /**
     * Méthode principale appelée à chaque requête HTTP.
     * - Extrait le token JWT de l'en-tête "Authorization"
     * - Valide le token
     * - Charge les détails de l'utilisateur
     * - Met à jour le contexte de sécurité Spring
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1- Extraction du token JWT
            String token = getJWTFromToken(request);
            // 2- Validation du token
            if (token != null && jwtUtils.validateToken(token)) {
                // 3- Extraction du username du token
                String email = jwtUtils.getUserNameFromJwtToken(token);
                // 4- Chargement des details utilisateurs
                UserDetails userDetails = userService.loadUserByUsername(email);
                // 5- Creation du contexte d'authentification
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 6- Definition du contexte de securite
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        // 7- chaîne de filtre
        filterChain.doFilter(request,response);
    }

    /**
     * Récupère le token JWT depuis l'en-tête HTTP "Authorization".
     * Format attendu : "Bearer <token>"
     *
     * @param //request La requête HTTP
     * @return Le token JWT ou null si non trouvé
     */
    public String getJWTFromToken(HttpServletRequest httpServletRequest) {
        final String bearerToken = httpServletRequest.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7,bearerToken.length());
        }
        return null;
    }
}