package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.ClienteValidationException;
import lombok.extern.slf4j.Slf4j;
import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.model.dto.ReservaRequestDTO;
import com.example.demo.repository.dao.ClienteRepository;
import com.example.demo.repository.entity.Cliente;

import jakarta.persistence.EntityNotFoundException;

@Service
@Slf4j
public class ClienteServiceImpl implements ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Busca un cliente existente por email. Si no existe, lo crea y lo guarda. Se
	 * implementa así para simplificar la lógica de Reserva.
	 * 
	 * @param request DTO de solicitud de reserva que contiene los datos del
	 *                cliente.
	 * @return ClienteDTO persistido (existente o nuevo).
	 */
	public ClienteDTO findOrCreate(ReservaRequestDTO request) {

		// 1. Intentar buscar por correo electrónico
		Optional<Cliente> cliExistente = clienteRepository.findByEmail(request.getCorreoElec());

		if (cliExistente.isPresent()) {
			// Cliente encontrado, lo devolvemos para usarlo en la Reserva.
			return ClienteDTO.convertToDTO(cliExistente.get());
		}

		// 2. Cliente no encontrado, creamos uno nuevo
		Cliente cliNuevo = new Cliente();
		cliNuevo.setNombre(request.getNombreCliente());
		cliNuevo.setEmail(request.getCorreoElec());
		cliNuevo.setTelf(request.getTelf());

		// 3. Guardamos el nuevo cliente y lo devolvemos
		return ClienteDTO.convertToDTO(clienteRepository.save(cliNuevo));
	}

	@Override
	public ClienteDTO findByEmailAndPass(String email, String pass) {
		Cliente cliente = null;

		// 1. Busca el cliente por email y lanza EntityNotFoundException si no existe
		cliente = clienteRepository.findByEmail(email)
				// Usa el mismo mensaje para no dar pistas sobre si falló el email o el pass
				.orElseThrow(() -> new EntityNotFoundException("Email o contraseña incorrectos."));

		// 2. Verifica la contraseña y lanza una excepción si no coincide
		if (!passwordEncoder.matches(pass, cliente.getPass())) {
			// Lanza EntityNotFoundException si la contraseña es incorrecta
			throw new EntityNotFoundException("Email o contraseña incorrectos.");
		}

		// 3. Si todo es correcto, devuelve el DTO
		return ClienteDTO.convertToDTO(cliente);
	}

	@Override
	public void guardarCliente(ClienteDTO clienteDTO) {
		try {
			log.info("Iniciando registro de cliente: {}", clienteDTO.getEmail());
			validarCliente(clienteDTO);

			// Si pasa las validaciones sin lanzar excepción, procedemos a guardar
			Cliente cliente = ClienteDTO.convertToEntity(clienteDTO);
			cliente.setPass(passwordEncoder.encode(clienteDTO.getPass()));
			clienteRepository.save(cliente);
			log.info("Cliente registrado correctamente: {}", clienteDTO.getEmail());

		} catch (ClienteValidationException e) {
			log.error("Error de validación al registrar cliente: {}", e.getMessage());
			throw e; // Propagamos la excepción para que el controlador la maneje
		} catch (Exception e) {
			log.error("Error inesperado al guardar el cliente: {}", e.getMessage(), e);
			throw new ClienteValidationException("Error inesperado al guardar el cliente: " + e.getMessage());
		}
	}

	private void validarCliente(ClienteDTO clienteDTO) {
		StringBuilder errores = new StringBuilder();

		// 1. Validación de nulidad del DTO
		if (clienteDTO == null) {
			throw new ClienteValidationException("Los datos del cliente no pueden ser nulos");
		}

		// 2. Validación de campos obligatorios
		if (isEmptyOrNull(clienteDTO.getNombre())) {
			errores.append("El nombre es obligatorio. ");
		}
		if (isEmptyOrNull(clienteDTO.getEmail())) {
			errores.append("El correo electrónico es obligatorio. ");
		}
		if (isEmptyOrNull(clienteDTO.getPass())) {
			errores.append("La contraseña es obligatoria. ");
		}

		// 3. Validación de formato de correo electrónico
		if (!isEmptyOrNull(clienteDTO.getEmail()) && !clienteDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
			errores.append("El formato del correo electrónico no es válido. ");
		}

		// 4. Validación de formato de teléfono (9 dígitos) - Solo si se proporciona
		if (!isEmptyOrNull(clienteDTO.getTelf()) && !clienteDTO.getTelf().matches("\\d{9}")) {
			errores.append("Si proporciona un teléfono, debe tener 9 dígitos. ");
		}
		// 5. Validación de contraseña: seguir misma regla que en Angular
		// Mínimo 8 caracteres, al menos una letra y un número
		if (!isEmptyOrNull(clienteDTO.getPass())) {
			String pass = clienteDTO.getPass();
			String passRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
			if (!pass.matches(passRegex)) {
				errores.append(
						"La contraseña debe tener al menos 8 caracteres, incluir al menos una letra y un número. ");
			}
		}

		// 6. Validación de correo electrónico único
		if (!isEmptyOrNull(clienteDTO.getEmail()) &&
				clienteRepository.findByEmail(clienteDTO.getEmail()).isPresent()) {
			errores.append("El correo electrónico ya está registrado. ");
		}

		// Si hay errores, lanzamos la excepción con todos los mensajes
		if (errores.length() > 0) {
			throw new ClienteValidationException(errores.toString().trim());
		}
	}

	private boolean isEmptyOrNull(String str) {
		return str == null || str.trim().isEmpty();
	}
}
