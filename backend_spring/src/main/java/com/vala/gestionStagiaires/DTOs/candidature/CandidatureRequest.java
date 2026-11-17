package com.vala.gestionStagiaires.DTOs.candidature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vala.gestionStagiaires.enums.Sexe;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dépôt ou modification d'une candidature. Les contraintes reprennent celles
 * du stagiaire, pour qu'une candidature acceptée produise un stagiaire valide.
 */
public record CandidatureRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 3, max = 40, message = "Le nom doit contenir entre 3 et 40 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 3, max = 40, message = "Le prénom doit contenir entre 3 et 40 caractères")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
        String telephone,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @NotNull(message = "Le sexe doit être spécifié")
        Sexe sexe,

        @Size(max = 100, message = "L'organisme ne doit pas dépasser 100 caractères")
        String organisme,

        @Size(max = 50, message = "Le niveau ne doit pas dépasser 50 caractères")
        String niveau,

        @Size(max = 100, message = "La filière ne doit pas dépasser 100 caractères")
        String filiere,

        Long departementSouhaiteId,

        LocalDate dateDebutSouhaitee,

        LocalDate dateFinSouhaitee,

        @Size(max = 4000, message = "La lettre de motivation ne doit pas dépasser 4000 caractères")
        String motivation) {

    @JsonIgnore
    @AssertTrue(message = "La date de début souhaitée doit être antérieure à la date de fin souhaitée")
    public boolean isPeriodeCoherente() {
        return dateDebutSouhaitee == null || dateFinSouhaitee == null || dateDebutSouhaitee.isBefore(dateFinSouhaitee);
    }
}
