package com.vala.gestionStagiaires.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Collection;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Encadrant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 3, max = 40,message = "Le nom ne doit pas dépasser 40 caractères")
    private String nom;

    @Size(min = 3, max = 40, message = "Le prénom ne doit pas depasser 40 caractères")
    private String prenom;

    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[-. ]?)?\\d{10}$", message = "Le numéro doit contenir 10 chiffres, avec un indicatif optionnel")
    private String telephone;

    @OneToMany(mappedBy = "encadrant")
    private Collection<Stage> stages;




}
