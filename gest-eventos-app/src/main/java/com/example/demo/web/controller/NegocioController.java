package com.example.demo.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.example.demo.exception.DominioSyncException;
import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.repository.entity.Negocio;
import com.example.demo.service.NegocioService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
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

    // Excepciones controladas por el GlobalExceptioHandler
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NegocioDTO negocioDTO) {
        try {
            negocioService.update(negocioDTO);
            return ResponseEntity.ok().build();
        } catch (DominioSyncException e) {
            // Solo loggear mensaje, no stacktrace
            log.warn("Error sincronizando dominio: {}", e.getDominio());
            return ResponseEntity.status(422).body(Map.of(
                    "error", "DOMINIO_DUPLICADO",
                    "message", e.getMessage(),
                    "dominio", e.getDominio()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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