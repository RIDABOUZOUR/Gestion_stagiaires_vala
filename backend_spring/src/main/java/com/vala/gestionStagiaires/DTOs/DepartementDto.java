package com.vala.gestionStagiaires.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartementDto {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, max = 40, message = "Le nom doit contenir entre 3 et 40 caractères")
    private String nom;

    @NotBlank(message = "Le responsable est obligatoire")
    @Size(min = 3, max = 40, message = "Le nom du responsable doit contenir entre 3 et 40 caractères")
    private String responsable;

}
