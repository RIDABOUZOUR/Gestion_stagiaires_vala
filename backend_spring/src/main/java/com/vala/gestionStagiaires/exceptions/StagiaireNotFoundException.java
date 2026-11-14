package com.vala.gestionStagiaires.exceptions;

public class StagiaireNotFoundException extends ResourceNotFoundException {

    public StagiaireNotFoundException(Long id) {
        super("Stagiaire", id);
    }

    public StagiaireNotFoundException(Throwable cause) {
        super(cause);
    }
}
