package com.vala.gestionStagiaires.controller;


import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.service.EncadrantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EncadrantController {
    @Autowired
    private EncadrantService encadrantService;

    @GetMapping("/encadrants")
    public ResponseEntity<List<EncadrantDto>> getEncadrant(){
        return ResponseEntity.ok(encadrantService.getEncadrant());
    }

    @PostMapping("/encadrants")
    public ResponseEntity<EncadrantDto> addStage(@Valid @RequestBody EncadrantDto encadrantDto){
        EncadrantDto created = encadrantService.addEncadrant(encadrantDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/encadrants/search")
    public ResponseEntity<List<EncadrantDto>> searchEncadrant(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return ResponseEntity.ok(encadrantService.searchEncadrant(keyword));
    }

    @DeleteMapping("/encadrants/{id}")
    public ResponseEntity<Void> deleteEncadrant(@PathVariable Long id){
        encadrantService.deleteEncadrant(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/encadrants/{id}")
    public ResponseEntity<EncadrantDto> updateEncadrant(@Valid @RequestBody EncadrantDto encadrantDto, @PathVariable Long id){
        return ResponseEntity.ok(encadrantService.updateEncadrant(encadrantDto,id));
    }

    @GetMapping("/encadrants/{id}")
    public ResponseEntity<EncadrantDto> getEncadrantById(@PathVariable Long id){
        return ResponseEntity.ok(encadrantService.getEncadrantById(id));
    }

}
