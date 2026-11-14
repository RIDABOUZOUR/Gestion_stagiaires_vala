package com.vala.gestionStagiaires.DTOs;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EncadrantDto {
    private Long id;

    @Size(min = 3, max = 40, message = "Le nom ne doit pas dépasser 40 caractères")
    private String nom;

    @Size(min = 3, max = 40, message = "Le prénom ne doit pas depasser 40 caractères")
    private String prenom;

    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
    private String telephone;

}
