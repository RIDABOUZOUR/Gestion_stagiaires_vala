package com.vala.gestionStagiaires.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Gestion centralisée des exceptions de l'API.
 * Transforme les erreurs en réponses JSON homogènes avec le bon code HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Erreurs de validation des DTO (@Valid) → HTTP 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Erreur de validation", errors);
    }

    /** Corps de requête illisible / JSON malformé (ex. date invalide) → HTTP 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Corps de requête illisible ou format invalide.", null);
    }

    /** Ressource inexistante → HTTP 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** Filet de sécurité pour les accès directs à Optional.orElseThrow() → HTTP 404. */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElement(NoSuchElementException ex) {
        return build(HttpStatus.NOT_FOUND, "Ressource introuvable", null);
    }

    /** Contraintes d'intégrité (doublon, clé étrangère) → HTTP 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT,
                "Conflit de données : contrainte d'intégrité violée (valeur déjà existante ou ressource encore référencée).",
                null);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Object errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        if (errors != null) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
