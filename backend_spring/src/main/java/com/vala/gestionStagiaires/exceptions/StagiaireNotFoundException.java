package com.vala.gestionStagiaires.exceptions;

public class StagiaireNotFoundException extends RuntimeException {

    public StagiaireNotFoundException(Long id) {
        super("Stagiaire avec l'identifiant " + id + " introuvable.");
    }

//    public StagiaireNotFoundException(String nom) {
//        super("Stagiaire avec le nom \"" + nom + "\" introuvable.");
//    }
//
//    public StagiaireNotFoundException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    public StagiaireNotFoundException(String message) {
//        super(message);
//    }

    public StagiaireNotFoundException(Throwable cause) {
        super(cause);
    }
}

