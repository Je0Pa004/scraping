package com.projet.scraping.Exeption;

public class ResourceNotFoundException extends RuntimeException {


    private String message;
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
