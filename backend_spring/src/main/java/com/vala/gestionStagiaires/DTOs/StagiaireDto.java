package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.enums.Sexe;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class StagiaireDto {
    private Long id;
    private String nom;
    private String prenom;

    private Date dateNaissance;

    private Sexe sexe;

    private String email;

    private String telephone;

    private String adresse;

    private String organisme;

    private StageDto stage;

    private String niveau;

    private String filiere;

    private DepartementDto departement;

}
