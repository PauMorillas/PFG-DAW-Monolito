package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.io.IOException;
import java.util.HashMap;

import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.service.GerenteService;

import jakarta.servlet.http.HttpServletResponse;

import com.example.demo.exception.ValidationException;

@RestController
public class GerenteController {

	@Autowired
	private GerenteService gerenteService;

	public ModelAndView getGerenteById() {

		GerenteDTO gerenteDTO = new GerenteDTO();
		ModelAndView mav = new ModelAndView();
		mav.addObject("gerenteDTO", gerenteDTO);

		return mav;
	}

	// Lógica: Sirve la página de registro con un dto vacío para rellenar con la
	// informacion del formulario
	@GetMapping("/register")
	public void register(HttpServletResponse response) throws IOException {
		response.sendRedirect("http://localhost:4200/registro-gerente");
	}

	// TODO: Hacerlo REST
	// Lógica: Procesa el formulario y registra el nuevo Gerente.
	@PostMapping("/api/gerentes/registro")
	public ResponseEntity<?> registrarGerente(@RequestBody GerenteDTO gerenteDTO) {
		try {
			// Llama al servicio para manejar la lógica de negocio (encriptación y guardado)
			gerenteService.save(gerenteDTO);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Gerente registrado con éxito");
			return ResponseEntity.ok(response);

		} catch (ValidationException ex) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", ex.getMessage());
			return ResponseEntity.badRequest().body(response);
		} catch (Exception ex) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error interno del servidor: " + ex.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

}
