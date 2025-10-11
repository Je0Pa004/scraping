package com.projet.scraping.Exeption;

public class AlreadyExistException extends RuntimeException {

    public AlreadyExistException(String message){
        super(message);
    }
}
