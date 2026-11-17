package com.vala.gestionStagiaires.DTOs;

import jakarta.validation.constraints.NotBlank;
import com.vala.gestionStagiaires.enums.Sexe;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;


@Data
public class StagiaireDto {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, max = 40, message = "Le nom doit contenir entre 3 et 40 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 3, max = 40, message = "Le prénom doit contenir entre 3 et 40 caractères")
    private String prenom;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le sexe doit être spécifié")
    private Sexe sexe;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
    private String telephone;

    @Size(max = 200, message = "L'adresse ne doit pas dépasser 200 caractères")
    private String adresse;

    private String organisme;

    @NotNull(message = "Le stage est obligatoire")
    private StageDto stage;

    private String niveau;

    private String filiere;

    @NotNull(message = "Le département est obligatoire")
    private DepartementDto departement;

}
