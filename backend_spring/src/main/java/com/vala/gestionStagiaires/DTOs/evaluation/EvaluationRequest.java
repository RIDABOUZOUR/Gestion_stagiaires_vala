package com.vala.gestionStagiaires.DTOs.evaluation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Corps de {@code PUT /stagiaires/{id}/evaluation}. */
public record EvaluationRequest(
        @NotNull(message = "La note globale est obligatoire")
        @DecimalMin(value = "0.0", message = "La note globale doit être comprise entre 0 et 20")
        @DecimalMax(value = "20.0", message = "La note globale doit être comprise entre 0 et 20")
        @Digits(integer = 2, fraction = 1, message = "La note globale accepte une seule décimale")
        BigDecimal noteGlobale,

        @NotNull(message = "La compétence technique est obligatoire")
        @Min(value = 1, message = "Les compétences sont notées de 1 à 5")
        @Max(value = 5, message = "Les compétences sont notées de 1 à 5")
        Integer competenceTechnique,

        @NotNull(message = "La communication est obligatoire")
        @Min(value = 1, message = "Les compétences sont notées de 1 à 5")
        @Max(value = 5, message = "Les compétences sont notées de 1 à 5")
        Integer communication,

        @NotNull(message = "L'autonomie est obligatoire")
        @Min(value = 1, message = "Les compétences sont notées de 1 à 5")
        @Max(value = 5, message = "Les compétences sont notées de 1 à 5")
        Integer autonomie,

        @NotNull(message = "L'assiduité est obligatoire")
        @Min(value = 1, message = "Les compétences sont notées de 1 à 5")
        @Max(value = 5, message = "Les compétences sont notées de 1 à 5")
        Integer assiduite,

        @Size(max = 2000, message = "L'appréciation ne doit pas dépasser 2000 caractères")
        String appreciation,

        @NotNull(message = "Indiquez si le stagiaire est recommandé")
        Boolean recommande) {
}
