package com.vala.gestionStagiaires.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /** Bean minimal possédant une propriété {@code nom} pour construire un BindingResult. */
    static class Sample {
        private String nom;

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }
    }

    @SuppressWarnings("unused")
    private void target(String value) {
        // Méthode support pour construire un MethodParameter valide.
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("target", String.class);
        return new MethodParameter(method, 0);
    }

    @Test
    @DisplayName("validation -> 400 avec le détail des champs")
    @SuppressWarnings("unchecked")
    void handleValidation_shouldReturn400WithFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Sample(), "dto");
        bindingResult.rejectValue("nom", "Size", "Le nom est trop court");
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat((Map<String, String>) response.getBody().get("errors"))
                .containsEntry("nom", "Le nom est trop court");
    }

    @Test
    @DisplayName("corps illisible -> 400")
    void handleUnreadable_shouldReturn400() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("bad json", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<Map<String, Object>> response = handler.handleUnreadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
    }

    @Test
    @DisplayName("ResourceNotFoundException -> 404")
    void handleNotFound_shouldReturn404() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Département", 99L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("message", "Département avec l'identifiant 99 introuvable.");
    }

    @Test
    @DisplayName("NoSuchElementException -> 404")
    void handleNoSuchElement_shouldReturn404() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNoSuchElement(new NoSuchElementException("absent"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "Ressource introuvable");
    }

    @Test
    @DisplayName("DataIntegrityViolationException -> 409")
    void handleDataIntegrity_shouldReturn409() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("dup"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
    }
}
