package com.vala.gestionStagiaires.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Gestion centralisée des exceptions de l'API.
 * Transforme les erreurs en réponses JSON homogènes avec le bon code HTTP.
 */
@Slf4j
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

    /** Paramètre de requête au mauvais format (ex. status=INCONNU, id=abc) → HTTP 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Paramètre invalide : " + ex.getName() + ".", null);
    }

    /** Tri sur une propriété inexistante (ex. sort=inconnu) → HTTP 400. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReference(PropertyReferenceException ex) {
        return build(HttpStatus.BAD_REQUEST, "Tri invalide : propriété inconnue « " + ex.getPropertyName() + " ».", null);
    }

    /** Identifiants de connexion incorrects → HTTP 401 (message volontairement générique). */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    /** Fichier envoyé refusé (CV absent, vide, pas en PDF…) → HTTP 400. */
    @ExceptionHandler(FichierInvalideException.class)
    public ResponseEntity<Map<String, Object>> handleFichierInvalide(FichierInvalideException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** Partie manquante d'une requête multipart (ex. « candidature ») → HTTP 400. */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handlePartieManquante(MissingServletRequestPartException ex) {
        return build(HttpStatus.BAD_REQUEST, "Partie manquante dans la requête : " + ex.getRequestPartName() + ".", null);
    }

    /** Fichier au-delà de la limite d'upload → HTTP 413. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTropVolumineux(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Le fichier dépasse 5 Mo.", null);
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

    /** Règle métier violée (transition interdite, ressource encore référencée…) → HTTP 409. */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** Contraintes d'intégrité (doublon, clé étrangère) → HTTP 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT,
                "Conflit de données : contrainte d'intégrité violée (valeur déjà existante ou ressource encore référencée).",
                null);
    }

    /** Paramètre de requête obligatoire absent (ex. ?keyword=) → HTTP 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleParametreManquant(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Paramètre manquant : " + ex.getParameterName() + ".", null);
    }

    /** Contrainte de validation sur un paramètre ou un objet hors @RequestBody → HTTP 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String chemin = violation.getPropertyPath().toString();
            errors.put(chemin.substring(chemin.lastIndexOf('.') + 1), violation.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Erreur de validation", errors);
    }

    /**
     * Refus d'un {@code @PreAuthorize} → HTTP 403, au même format que la chaîne de sécurité.
     * Déclaré explicitement pour que le filet {@link #handleInattendue} ne le transforme pas en 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccesRefuse(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Accès refusé : vos droits ne permettent pas cette action.", null);
    }

    /**
     * Filet de sécurité : toute autre exception garde le format JSON de l'API.
     * Les erreurs du framework (méthode non supportée, type de contenu refusé, route inconnue…)
     * conservent leur code HTTP ; le reste devient un 500 sans détail technique.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInattendue(Exception ex) {
        if (ex instanceof ErrorResponse erreurFramework) {
            HttpStatusCode code = erreurFramework.getStatusCode();
            HttpStatus status = HttpStatus.resolve(code.value());
            String message = status != null ? status.getReasonPhrase() : "Requête refusée";
            return build(status != null ? status : HttpStatus.BAD_REQUEST, message, null);
        }
        log.error("Erreur inattendue", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur.", null);
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
