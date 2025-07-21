package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.service.DepartementService;

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

    @GetMapping("/departements/search")
    public List<DepartementDto> searchDepartement(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return departementService.searchDepartement(keyword);
    }

    @PostMapping("/departements")
    public Departement addDepartement(@RequestBody DepartementDto departementDto){
        return departementService.addDepartement(departementDto);
    }

    @DeleteMapping("/departements/{id}")
    public void deleteDepartement(@PathVariable Long id){
        departementService.deleteDepartement(id);
    }

    @PutMapping("/departements/{id}")
    public Departement updateDepartement(@RequestBody DepartementDto departementDto ,@PathVariable Long id){
        return departementService.updateDepartement(departementDto,id);
    }
    @GetMapping("/departements/{id}")
    public DepartementDto getDepartementById(@PathVariable Long id){
        return departementService.getDepartementById(id);
    }
}

