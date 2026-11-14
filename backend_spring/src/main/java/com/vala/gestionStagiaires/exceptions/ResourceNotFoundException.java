package com.vala.gestionStagiaires.exceptions;

/**
 * Exception levée lorsqu'une ressource demandée n'existe pas (→ HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " avec l'identifiant " + id + " introuvable.");
    }

    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }
}
