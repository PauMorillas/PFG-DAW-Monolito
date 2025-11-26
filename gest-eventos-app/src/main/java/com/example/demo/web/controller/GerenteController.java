package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.model.dto.LoginRequestDTO;
import com.example.demo.model.dto.LoginResponseDTO;
import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.service.GerenteService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;

import com.example.demo.exception.ValidationException;

@RestController
public class GerenteController {

	@Autowired
	private GerenteService gerenteService;

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

	@PostMapping("/api/gerentes/login")
	public ResponseEntity<?> login(@RequestBody LoginRequestDTO req) {
		try {
			LoginResponseDTO loginRespDTO = gerenteService.login(req);
			return ResponseEntity.ok(loginRespDTO);
		} catch (EntityNotFoundException ex) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", ex.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		} catch (Exception ex) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error interno del servidor: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/api/gerentes/{email}/negocios")
	public ResponseEntity<List<NegocioDTO>> getNegociosPorGerente(@PathVariable String email) {
		GerenteDTO gerenteOpt = null;
		try {
			gerenteOpt = gerenteService.findByEmail(email);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(gerenteOpt.getListaNegociosDTO());
	}

	/**
     * Endpoint para actualizar completamente un Gerente.
     * Corresponde a la llamada PUT /api/gerentes/{id}
     */
    @PutMapping("/api/gerentes/editar") // {id} mapea a la variable PathVariable
    public ResponseEntity<Map<String, Object>> actualizarGerente(
            @RequestBody GerenteDTO gerenteDTO) {

        try {
            gerenteService.update(gerenteDTO); // Asume que tu service tiene un método update

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Gerente actualizado correctamente");
            // Devolver 200 OK para una actualización exitosa
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ValidationException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno al actualizar el Gerente: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}
