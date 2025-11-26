package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Estado;
import com.example.demo.model.dto.ReservaDTO;
import com.example.demo.service.ReservaService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservaById(@PathVariable Long id) {
        try {
            ReservaDTO reserva = reservaService.findById(id);
            return ResponseEntity.ok(reserva);
        } catch (EntityNotFoundException e) {
            // Devolver 404 si la reserva no se encuentra
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Manejar otros errores con 500
            return ResponseEntity.internalServerError().body("Error interno al obtener la reserva.");
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateEstadoReserva(
            @PathVariable Long id,
            @RequestParam Estado estado)
    {
        try {
            ReservaDTO reservaDTO = reservaService.updateEstado(id, estado);
            return ResponseEntity.ok(reservaDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("No se pudo actualizar el estado.");
        }
    }
}