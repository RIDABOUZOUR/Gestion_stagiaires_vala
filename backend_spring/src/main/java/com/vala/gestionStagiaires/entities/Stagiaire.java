package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.Sexe;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

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

    private String nom;

    private String prenom;

    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    private String email;

    private String telephone;

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
