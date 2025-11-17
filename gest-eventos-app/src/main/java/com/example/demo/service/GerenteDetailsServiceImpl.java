package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.repository.dao.GerenteRepository;
import com.example.demo.repository.entity.Gerente;

@Service
public class GerenteDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private GerenteRepository gerenteRepository;

	@Override
	public UserDetails loadUserByUsername(String correoElec) throws UsernameNotFoundException {
		Gerente gerente = gerenteRepository.findByEmail(correoElec)
				.orElseThrow(() -> new UsernameNotFoundException("Gerente no encontrado con email: " + correoElec));

		// Mapea la entidad Gerente a un objeto UserDetails que Spring Security entienda.
		// Formato: correo y contraseña y rol de gestor
		return User.builder().username(gerente.getEmail())
				.password(gerente.getPass()) // Paso invisible: Spring Security Hace la Comparación
												// utilizando el bean definido en SecurityConfig: @Bean public PasswordEncoder,
												// realiza la siguiente operación interna: passwordEncoder.matches("pass123"
												// (Texto Plano del Formulario), "$2a$xxx..." (Hash de la DB));
				.roles("GESTOR")
				.build();
	}
}
