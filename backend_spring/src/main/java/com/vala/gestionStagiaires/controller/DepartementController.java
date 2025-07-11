package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.service.DepartementService;
import com.vala.gestionStagiaires.service.EncadrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController
public class DepartementController {
    @Autowired
    private DepartementService departementService;

    @GetMapping("/departements")
    public List<DepartementDto> getDepartement(){
        return departementService.getDepartement();
    }
    @PostMapping("/departements")
    public Departement addDepartement(@RequestBody DepartementDto departementDto){
        return departementService.addDepartement(departementDto);
    }
    @GetMapping("/departements/search")
    public List<DepartementDto> searchDepartement(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return departementService.searchDepartement(keyword);
    }
}
