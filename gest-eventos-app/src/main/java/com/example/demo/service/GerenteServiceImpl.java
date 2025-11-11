package com.example.demo.service;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.ValidationException;
import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.repository.dao.GerenteRepository;
import com.example.demo.repository.entity.Gerente;

@Service
public class GerenteServiceImpl implements GerenteService {
	// RegEx para validar teléfono español (9 dígitos, empieza por 6 o 7)
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[67]\\d{8}$");

	@Autowired
	private GerenteRepository gerenteRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void save(GerenteDTO gerenteDTO) {
		if (validarDatosGerente(gerenteDTO)) {
			// 1. Mapeo a Entidad
			Gerente gerente = GerenteDTO.convertToEntity(gerenteDTO);

			// 2. Encriptación de la contraseña
			String hashedPassword = passwordEncoder.encode(gerenteDTO.getPass());
			gerente.setPass(hashedPassword);

			// 3. Persistimos la entidad en la BD
			gerenteRepository.save(gerente);
		}
	}

	private boolean validarDatosGerente(GerenteDTO gerenteDTO) throws ValidationException {
		boolean valido = true;
		// Validación 1: Correo Electrónico Único
		if (gerenteRepository.findByCorreoElec(gerenteDTO.getCorreoElec()).isPresent()) {
			// Si el email existe, lanzamos la excepción con el mensaje de error
			valido = false;
			throw new ValidationException("El correo electrónico ya está registrado. Por favor, utiliza otro.");
		}

		// Validación 2: Contraseña no vacía y longitud mínima
		if (gerenteDTO.getPass() == null || gerenteDTO.getPass().trim().length() < 6) {
			valido = false;
			throw new ValidationException("La contraseña debe tener al menos 6 caracteres.");
		}
		
		// Validación 3: Teléfono no nulo
        if (gerenteDTO.getTelf() == null || gerenteDTO.getTelf().trim().isEmpty()) {
        	valido = false;
            throw new ValidationException("El número de teléfono es obligatorio.");
        }
        
        // Validación 4: Formato de Teléfono
        if (!PHONE_PATTERN.matcher(gerenteDTO.getTelf()).matches()) {
        	valido = false;
            throw new ValidationException("El número de teléfono debe ser un formato móvil español válido (9 dígitos, comenzando por 6 o 7).");
        }

		return valido;
	}
}
