package com.projet.scraping.Exeption;

import com.projet.scraping.utils.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleInternalError(Exception ex) {

        BaseResponse<String> response = new BaseResponse<>();
        response.setCode(500);
        response.setDescription("Erreur interne du serveur");
        response.setData(ex.getMessage()); // pour debug — optionnel en prod
        return ResponseEntity.status(500).body(response);
    }

}
