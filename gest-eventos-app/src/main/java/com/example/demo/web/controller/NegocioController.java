package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.repository.entity.Negocio;
import com.example.demo.service.NegocioService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    @Autowired
    private NegocioService negocioService;

    // Obtener negocio por ID
    @GetMapping("/{id}")
    public ResponseEntity<NegocioDTO> getById(@PathVariable Long id) {
        try {
            NegocioDTO negocioDTO = negocioService.findById(id);
            return ResponseEntity.ok(negocioDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Crear nuevo negocio
    @PostMapping("/create")
    public ResponseEntity<NegocioDTO> postMethodName(@RequestBody NegocioDTO negocioDTO) {
        try {
            negocioService.save(negocioDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NegocioDTO> update(@PathVariable Long id, @RequestBody NegocioDTO negocioDTO) {
        try {
            negocioService.update(negocioDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            negocioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}