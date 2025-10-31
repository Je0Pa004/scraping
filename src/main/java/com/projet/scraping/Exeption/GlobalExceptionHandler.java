package com.projet.scraping.Exeption;

import com.projet.scraping.utils.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        BaseResponse<String> response = new BaseResponse<>();
        response.setCode(404);
        response.setDescription("Ressource non trouvée");
        response.setData(ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<String>> handleIllegalArgument(IllegalArgumentException ex) {
        BaseResponse<String> response = new BaseResponse<>();
        response.setCode(400);
        response.setDescription("Argument invalide");
        response.setData(ex.getMessage());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleInternalError(Exception ex) {
        BaseResponse<String> response = new BaseResponse<>();
        response.setCode(500);
        response.setDescription("Erreur interne du serveur");
        response.setData(ex.getMessage()); // pour debug — optionnel en prod
        ex.printStackTrace(); // Log pour debug
        return ResponseEntity.status(500).body(response);
    }

}
