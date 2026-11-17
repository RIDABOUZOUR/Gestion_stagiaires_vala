package com.vala.gestionStagiaires.exceptions;

/**
 * Violation d'une règle métier (transition de statut interdite, suppression
 * d'une ressource encore référencée, affectation impossible…) → HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
