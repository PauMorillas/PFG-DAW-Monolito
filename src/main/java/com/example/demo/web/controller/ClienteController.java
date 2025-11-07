package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    private ClienteService clienteService;

    // TODO: Cambiar a configuracion cors dinamica
    @CrossOrigin(origins = "http://localhost:4200") // (Crucial para que Angular se conecte)
    @PostMapping("/registro") // Endpoint específico para recibir el POST
    public ResponseEntity<Boolean> registrarCliente(@RequestBody ClienteDTO cliDTO) {
        
        // 2. LÓGICA DE NEGOCIO: Hashear la contraseña y guardar
        boolean isClienteGuardado = clienteService.guardarCliente(cliDTO);

        // 3. RESPUESTA: 201 Created si todo va bien
        return new ResponseEntity<>(isClienteGuardado, HttpStatus.CREATED);
    }
}
