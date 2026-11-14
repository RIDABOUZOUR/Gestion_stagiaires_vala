package com.vala.gestionStagiaires.entities;


import jakarta.persistence.*;
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
    private String nom;

    private String prenom;

    private String email;

    private String telephone;

    @OneToMany(mappedBy = "encadrant")
    private Collection<Stage> stages;




}
