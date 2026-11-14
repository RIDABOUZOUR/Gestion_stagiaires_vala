package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.enums.Sexe;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;


@Data
public class StagiaireDto {
    private Long id;

    @Size(min = 3, max = 40, message = "Le nom ne doit pas dépasser 40 caractères")
    private String nom;

    @Size(min = 3, max = 40, message = "Le prénom ne doit pas depasser 40 caractères")
    private String prenom;

    @Past(message = "La date de naissance doit être dans le passé")
    private Date dateNaissance;

    @NotNull(message = "Le sexe doit être spécifié")
    private Sexe sexe;

    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
    private String telephone;

    @Size(max = 200, message = "L'adresse ne doit pas dépasser 200 caractères")
    private String adresse;

    private String organisme;

    @Valid
    private StageDto stage;

    private String niveau;

    private String filiere;

    @Valid
    private DepartementDto departement;

}
