package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.StageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sujet;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StageStatus status;

    @OneToMany(mappedBy = "stage", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Stagiaire> stagiaires = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encadrant_id")
    private Encadrant encadrant;
}
