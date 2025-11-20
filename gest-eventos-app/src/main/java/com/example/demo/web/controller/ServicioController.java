package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.dto.ServicioDTO;
import com.example.demo.service.ServicioService;

import jakarta.persistence.EntityNotFoundException;

@RestController
public class ServicioController {

	@Autowired
	private ServicioService servicioService;

	// TODO: CRUD DE LOS SERVICIOS (DASHBOARD DE LOS Gerentes)

	@GetMapping("/api/servicios/{idServicio}")
	public ResponseEntity<ServicioDTO> findById(@PathVariable Long idServicio) {
		try {
			ServicioDTO servicioDTO = servicioService.findById(idServicio);
			return ResponseEntity.ok(servicioDTO);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
