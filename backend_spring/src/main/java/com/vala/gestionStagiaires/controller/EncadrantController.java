package com.vala.gestionStagiaires.controller;


import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.service.EncadrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController

public class EncadrantController {
    @Autowired
    private EncadrantService encadrantService;

    @GetMapping("/encadrants")
    public List<EncadrantDto> getEncadrant(){
        return encadrantService.getEncadrant();
    }
    @PostMapping("/encadrants")
    public Encadrant addStage(@RequestBody EncadrantDto encadrantDto){
        return encadrantService.addEncadrant(encadrantDto);
    }
    @GetMapping("/encadrants/search")
    public List<EncadrantDto> searchEncadrant(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return encadrantService.searchEncadrant(keyword);
    }

}
