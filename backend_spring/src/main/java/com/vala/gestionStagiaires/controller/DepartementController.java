package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.service.DepartementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepartementController {
    @Autowired
    private DepartementService departementService;

    @GetMapping("/departements")
    public ResponseEntity<List<DepartementDto>> getDepartement(){
        return ResponseEntity.ok(departementService.getDepartement());
    }

    @GetMapping("/departements/search")
    public ResponseEntity<List<DepartementDto>> searchDepartement(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return ResponseEntity.ok(departementService.searchDepartement(keyword));
    }

    @PostMapping("/departements")
    public ResponseEntity<DepartementDto> addDepartement(@Valid @RequestBody DepartementDto departementDto){
        DepartementDto created = departementService.addDepartement(departementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/departements/{id}")
    public ResponseEntity<Void> deleteDepartement(@PathVariable Long id){
        departementService.deleteDepartement(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/departements/{id}")
    public ResponseEntity<DepartementDto> updateDepartement(@Valid @RequestBody DepartementDto departementDto, @PathVariable Long id){
        return ResponseEntity.ok(departementService.updateDepartement(departementDto,id));
    }

    @GetMapping("/departements/{id}")
    public ResponseEntity<DepartementDto> getDepartementById(@PathVariable Long id){
        return ResponseEntity.ok(departementService.getDepartementById(id));
    }
}
