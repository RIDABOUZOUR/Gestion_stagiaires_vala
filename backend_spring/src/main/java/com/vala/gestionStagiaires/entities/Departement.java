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
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nom;

    private String responsable;

    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Stagiaire> stagiaires = new ArrayList<>();
}
