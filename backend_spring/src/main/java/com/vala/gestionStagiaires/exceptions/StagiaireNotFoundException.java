package com.vala.gestionStagiaires.exceptions;

public class StagiaireNotFoundException extends RuntimeException {

    public StagiaireNotFoundException(Long id) {
        super("Stagiaire avec l'identifiant " + id + " introuvable.");
    }

    public StagiaireNotFoundException(Throwable cause) {
        super(cause);
    }
}

