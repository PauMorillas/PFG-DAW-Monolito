package com.example.demo.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.ClienteValidationException;
import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.service.ClienteService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/clientes")
@Slf4j
public class ClienteController {
    @Autowired
    private ClienteService clienteService;

    // TODO: Cambiar a configuracion cors dinamica
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/registro") // Endpoint específico para recibir el POST
    public ResponseEntity<Map<String, Object>> registrarCliente(@RequestBody ClienteDTO cliDTO) {
        try {
            // Intentamos guardar el cliente
            clienteService.guardarCliente(cliDTO);
            
            // Si se guarda correctamente, devolverá 201 Created
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("success", true);
            response.put("message", "Cliente registrado correctamente");
            response.put("email", cliDTO.getEmail());
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (ClienteValidationException e) {
            // Error de validación, devolver 400 Bad Request con el mensaje específico
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorType", "VALIDATION_ERROR");
            
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            // Error inesperado, devolver 500 Internal Server Error
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            response.put("errorType", "INTERNAL_ERROR");
            
            log.error("Error inesperado en registro: {}", e.getMessage(), e);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
