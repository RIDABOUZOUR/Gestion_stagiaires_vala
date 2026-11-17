package com.vala.gestionStagiaires.exceptions;

/** Fichier envoyé refusé (absent, vide, trop lourd ou pas au bon format) → HTTP 400. */
public class FichierInvalideException extends RuntimeException {

    public FichierInvalideException(String message) {
        super(message);
    }
}
