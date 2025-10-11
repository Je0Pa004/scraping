package com.projet.scraping.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author davibil
 *
 * A class that implements Spring AuthenticationEntryPoint interface to handle
 * unauthorized access attempts to a protected resource. This implementation
 * sets the HTTP status code to 401 (unauthorized) and sends a message to the
 * client indicating that the access is denied along with the exception message.
 */

// Gestion des eerurs d'authentification
/**
 * Gère les cas où un utilisateur non authentifié essaie d'accéder à une ressource sécurisée.
 * Retourne une réponse HTTP 401 Unauthorized.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    /**
     * Called when an unauthorized access attempt to a protected resource is detected.
     * Sets the HTTP status code to 401 (unauthorized) and sends a message to the client indicating
     * that the access is denied along with the exception message.
     */
    /**
     * Méthode appelée quand une requête vers une ressource sécurisée
     * n'est pas authentifiée (ex: token manquant ou invalide).
     *
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @param authException L'exception levée lors de l'échec d'authentification
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("Access Denied! " + authException.getMessage());
    }
}
