package com.vala.gestionStagiaires.DTOs.candidature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefusRequest(
        @NotBlank(message = "Le motif du refus est obligatoire")
        @Size(max = 1000, message = "Le motif ne doit pas dépasser 1000 caractères")
        String motif) {
}
