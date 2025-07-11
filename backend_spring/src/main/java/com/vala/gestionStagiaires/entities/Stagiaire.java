package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.Sexe;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Stagiaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 40, message = "Le nom ne doit pas dépasser 40 caractères")
    private String nom;

    @Size(min = 3, max = 40, message = "Le prénom ne doit pas depasser 40 caractères")
    private String prenom;

    @Past(message = "La date de naissance doit être dans le passé")
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le sexe doit être spécifié")
    private Sexe sexe;

    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
    private String telephone;

    @Size(max = 200, message = "L'adresse ne doit pas dépasser 200 caractères")
    private String adresse;


    private String organisme;

    @ManyToOne
    @JoinColumn
    private Stage stage;

    private String niveau;

    private String filiere;

    @ManyToOne
    @JoinColumn
    private Departement departement;


}
