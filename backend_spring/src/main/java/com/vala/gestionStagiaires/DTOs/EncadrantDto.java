package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stage;
import lombok.Data;

import java.util.List;
@Data
public class EncadrantDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

}
