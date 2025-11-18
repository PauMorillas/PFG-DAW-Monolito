package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.service.NegocioService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    @Autowired(required = false)
    private NegocioService negocioService = null;

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
    @PostMapping("path")
    public String postMethodName(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
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

    /*
     * @PostMapping
     * public Negocio create(@RequestBody Negocio negocio) {
     * return negocioService.save(negocio);
     * }
     * 
     * // Actualizar negocio
     * 
     * @PutMapping("/{id}")
     * public ResponseEntity<Negocio> update(@PathVariable Long id, @RequestBody
     * Negocio negocioDetails) {
     * return negocioService.findById(id).map(negocio -> {
     * negocio.setNombre(negocioDetails.getNombre());
     * negocio.setCorreoElec(negocioDetails.getCorreoElec());
     * negocio.setTelfContacto(negocioDetails.getTelfContacto());
     * negocio.setHoraApertura(negocioDetails.getHoraApertura());
     * negocio.setHoraCierre(negocioDetails.getHoraCierre());
     * return ResponseEntity.ok(negocioService.save(negocio));
     * }).orElse(ResponseEntity.notFound().build());
     * }
     * 
     * // Borrar negocio
     * 
     * @DeleteMapping("/{id}")
     * public ResponseEntity<Void> delete(@PathVariable Long id) {
     * return negocioService.findById(id).map(negocio -> {
     * negocioService.delete(negocio);
     * return ResponseEntity.noContent().<Void>build();
     * }).orElse(ResponseEntity.notFound().build());
     * }
     * 
     * // Listar negocios por gerente
     * 
     * @GetMapping("/gerente/{idGerente}")
     * public List<Negocio> getByGerente(@PathVariable Long idGerente) {
     * return negocioService.findByGerenteId(idGerente);
     * }
     */
}