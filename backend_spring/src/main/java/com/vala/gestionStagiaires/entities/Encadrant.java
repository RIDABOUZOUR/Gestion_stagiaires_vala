package com.vala.gestionStagiaires.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "encadrant", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Stage> stages = new ArrayList<>();
}
