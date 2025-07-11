package com.vala.gestionStagiaires.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 40,message = "Le nom ne doit pas dépasser 40 caractères")
    private String nom;

    @Size(min = 3, max = 40,message = "Le nom du responsable ne doit pas dépasser 40 caractères")
    private String responsable;

    @OneToMany
    private List<Stagiaire> stagiaires;
}
