package com.vala.gestionStagiaires.entities;


import com.vala.gestionStagiaires.enums.StageStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 100, message = "Le sujet ne doit pas dépasser 100 caractères")
    @Column(unique = true)
    private String sujet;

    @Temporal(TemporalType.DATE)
    @NotNull(message = "La date de début est obligatoire")
    private Date dateDebut;

    @Temporal(TemporalType.DATE)
    @NotNull(message = "La date de fin est obligatoire")
    private Date dateFin;

    @Enumerated(EnumType.STRING)
    private StageStatus status;

    @OneToMany(mappedBy = "stage")
    private List<Stagiaire> stagiaire;


    @ManyToOne
    @JoinColumn
    private Encadrant encadrant;

    @AssertTrue(message = "La date de début doit être antérieure à la date de fin")
    public boolean isDateDebutBeforeDateFin() {
        if (dateDebut == null || dateFin == null) return true; // Laisse les autres annotations gérer le null
        return dateDebut.before(dateFin);
    }

}
