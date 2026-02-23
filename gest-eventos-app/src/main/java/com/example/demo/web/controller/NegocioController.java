package com.example.demo.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.DominioSyncException;
import com.example.demo.exception.EmailAlreadyInUseException;
import com.example.demo.model.dto.NegocioDTO;
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
    public ResponseEntity<?> save(@RequestBody NegocioDTO negocioDTO) {
        try {
            negocioService.save(negocioDTO);
            return ResponseEntity.ok().build();
        } catch (EmailAlreadyInUseException e) {
            // ERROR ESPERADO → correo duplicado
            return ResponseEntity.status(409).body(Map.of(
                    "error", "EMAIL_DUPLICADO",
                    "message", e.getMessage(),
                    "correo", negocioDTO.getCorreoElec()));
        } catch (EntityNotFoundException e) {
            // Gerente no encontrado
            return ResponseEntity.status(404).body(Map.of(
                    "error", "GERENTE_NO_ENCONTRADO",
                    "message", e.getMessage()));
        } catch (DominioSyncException e) {
            // Solo loggear mensaje, no stacktrace
            log.warn("Error sincronizando dominio: {}", e.getDominio());
            return ResponseEntity.status(422).body(Map.of(
                    "error", "DOMINIO_DUPLICADO",
                    "message", e.getMessage(),
                    "dominio", e.getDominio()));
        } catch (Exception e) {
            // Error inesperado
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "ERROR_INTERNO"));
        }
    }

    // Excepciones controladas por el GlobalExceptioHandler
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NegocioDTO negocioDTO) {
        try {
            negocioService.update(negocioDTO);
            return ResponseEntity.ok().build();
        } catch (DominioSyncException e) {
            log.warn("Error sincronizando dominio: {}", e.getDominio());
            return ResponseEntity.status(422).body(Map.of(
                    "error", "DOMINIO_DUPLICADO",
                    "message", e.getMessage(),
                    "dominio", e.getDominio()));
        } catch (DataIntegrityViolationException e) {
            log.warn("Correo duplicado en update: {}", negocioDTO.getCorreoElec());
            return ResponseEntity.status(409).body(Map.of(
                    "error", "EMAIL_DUPLICADO",
                    "message", "El correo ya está en uso",
                    "correo", negocioDTO.getCorreoElec()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            negocioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (EmailAlreadyInUseException e) {
            // ERROR ESPERADO → correo duplicado
            return ResponseEntity.status(409).body(Map.of(
                    "error", "EMAIL_DUPLICADO",
                    "message", e.getMessage()));
        } catch (EntityNotFoundException e) {
            // Gerente no encontrado
            return ResponseEntity.status(404).body(Map.of(
                    "error", "GERENTE_NO_ENCONTRADO",
                    "message", e.getMessage()));
        } catch (DominioSyncException e) {
            // Solo loggear mensaje, no stacktrace
            log.warn("Error sincronizando dominio: {}", e.getDominio());
            return ResponseEntity.status(422).body(Map.of(
                    "error", "DOMINIO_DUPLICADO",
                    "message", e.getMessage(),
                    "dominio", e.getDominio()));
        } catch (Exception e) {
            // Error inesperado
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "ERROR_INTERNO"));
        }
    }
}