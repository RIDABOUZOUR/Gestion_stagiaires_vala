package com.vala.gestionStagiaires.entities;


import com.vala.gestionStagiaires.enums.StageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
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

    @Column(unique = true)
    private String sujet;

    @Temporal(TemporalType.DATE)
    private Date dateDebut;

    @Temporal(TemporalType.DATE)
    private Date dateFin;

    @Enumerated(EnumType.STRING)
    private StageStatus status;

    @OneToMany(mappedBy = "stage")
    private List<Stagiaire> stagiaire;


    @ManyToOne
    @JoinColumn
    private Encadrant encadrant;

}
