package com.vala.gestionStagiaires.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EncadrantDto {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, max = 40, message = "Le nom doit contenir entre 3 et 40 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 3, max = 40, message = "Le prénom doit contenir entre 3 et 40 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
    private String telephone;

}
