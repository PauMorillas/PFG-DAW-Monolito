package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.ValidationException;
import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.model.dto.LoginRequestDTO;
import com.example.demo.model.dto.LoginResponseDTO;
import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.repository.dao.GerenteRepository;
import com.example.demo.repository.entity.Gerente;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GerenteServiceImpl implements GerenteService {
	// RegEx para validar teléfono español (9 dígitos, empieza por 6 o 7)
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[67]\\d{8}$");

	private static final Pattern PASS_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\\\d)[A-Za-z\\\\d]{8,}$");

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
		if (gerenteRepository.findByEmail(gerenteDTO.getCorreoElec()).isPresent()) {
			// Si el email existe, lanzamos la excepción con el mensaje de error
			valido = false;
			throw new ValidationException("El correo electrónico ya está registrado. Por favor, utiliza otro.");
		}

		// Validación 2: Contraseña no vacía y longitud mínima
		if (gerenteDTO.getPass() == null || gerenteDTO.getPass().trim().length() < 8) {
			valido = false;
			throw new ValidationException("La contraseña debe tener al menos 8 caracteres.");
		}

		if (!PASS_PATTERN.matcher(gerenteDTO.getPass()).matches()) {
			throw new ValidationException("La contraseña debe tener al menos una letra y un número.");
		}

		// Validación 3: Teléfono no nulo
		if (gerenteDTO.getTelf() == null || gerenteDTO.getTelf().trim().isEmpty()) {
			valido = false;
			throw new ValidationException("El número de teléfono es obligatorio.");
		}

		// Validación 4: Formato de Teléfono
		if (!PHONE_PATTERN.matcher(gerenteDTO.getTelf()).matches()) {
			valido = false;
			throw new ValidationException(
					"El número de teléfono debe ser un formato móvil español válido (9 dígitos, comenzando por 6 o 7).");
		}

		return valido;
	}

	@Override
	public GerenteDTO findByEmail(String email) {
		// Obtenemos el gerente de la base de datos
		Gerente gerente = gerenteRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Gerente no encontrado"));

		GerenteDTO gerenteDTO = GerenteDTO.convertToDTO(gerente);

		// Convertimos sus negocios a DTO (TODO: sin mapear servicios aún)
		List<NegocioDTO> listaNegociosDTO = new ArrayList<>();
		gerente.getListaNegocios().stream().map(negocio -> {
			// Mapear aqui los servicios
			NegocioDTO negocioDTO = NegocioDTO.convertToDTO(negocio, null, null);
			listaNegociosDTO.add(negocioDTO);
			return negocioDTO;
		}).collect(Collectors.toList());

		gerenteDTO.setListaNegociosDTO(listaNegociosDTO);

		return gerenteDTO;
	}

	@Override
	public LoginResponseDTO login(LoginRequestDTO req) {
		Gerente gerente = gerenteRepository.findByEmail(req.getEmail())
				.orElseThrow(() -> new EntityNotFoundException("El correo o la contraseña son incorrectos."));
		if (passwordEncoder.matches(req.getPassword(), gerente.getPass())) {
			return new LoginResponseDTO(gerente.getEmail(), gerente.getRol());
		} else {
			throw new EntityNotFoundException("El correo o la contraseña son incorrectos.");
		}
	}

	@Override
	public void update(GerenteDTO gerenteDTO) {
		// 1. Buscar la entidad existente por email
		Gerente gerenteExistente = gerenteRepository.findByEmail(gerenteDTO.getCorreoElec())
				.orElseThrow(() -> new EntityNotFoundException("Gerente no encontrado para actualizar."));
		
		gerenteExistente.setNombre(gerenteDTO.getNombre());
		gerenteExistente.setTelf(gerenteDTO.getTelf());

		if (gerenteDTO.getPass() != null && !gerenteDTO.getPass().isEmpty()) {
			String newPassHash = passwordEncoder.encode(gerenteDTO.getPass());
			gerenteExistente.setPass(newPassHash);
		}

		// 3. Guardar la entidad. Como ya es una entidad existente con un ID,
		// JPA realiza un UPDATE.
		gerenteRepository.save(gerenteExistente);
	}
}
