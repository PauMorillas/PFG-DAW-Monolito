package com.example.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Lazy;

import com.example.demo.service.DominioService;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Value("${api.secret}")
	private final String apiToken;

	@Value("${api.angular.url}")
	private final String urlAngular;

	public SecurityConfig(@Lazy DominioService dominioService, @Value("${api.secret}") String apiToken,
			@Value("${api.angular.url}") String urlAngular) {
		this.apiToken = apiToken;
		this.urlAngular = urlAngular;
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		try {
			restTemplate.getInterceptors().add((request, body, execution) -> {
				request.getHeaders().set("Authorization", "Bearer " + apiToken);
				request.getHeaders().set("Accept", "text/plain");
				System.out.println(this.urlAngular);
				log.info(this.urlAngular);
				return execution.execute(request, body);
			});
			return restTemplate;
		} catch (Exception e) {
			log.error("Error al obtener los dominios permitidos. Usando fallback a los dominios por defecto",
					e.getMessage());
			return restTemplate;
		}
	}

	// 1. Define el encriptador de contraseñas
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 2. Define la configuración CORS
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Convertimos la cadena separada por comas en lista
		List<String> allowedOrigins = Arrays.stream(urlAngular.split(","))
				.map(String::trim)
				.toList();
		configuration.setAllowedOrigins(allowedOrigins);

		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		source.registerCorsConfiguration("/public/api/**", configuration);
		return source;
	}

	// 3. Define la cadena de filtros de seguridad (Autorización)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiTokenFilter apiTokenFilter) throws Exception {
		http
				.addFilterBefore(apiTokenFilter, UsernamePasswordAuthenticationFilter.class)
				.cors()
				.and()
				/*
				 * .headers(headers -> headers
				 * .frameOptions(frameOptions -> frameOptions.disable()) // No bloquear iframes
				 * )
				 */
				.authorizeHttpRequests(auth -> auth
						// RUTAS PÚBLICAS
						.requestMatchers("/", "/public/**", "/login", "/register", "/css/**", "/js/**").permitAll()

						// API PRIVADA → REQUIERE AUTENTICACIÓN (Bearer)
						.requestMatchers("/api/**").authenticated()

						// Thymeleaf privadas
						.requestMatchers("/home", "/dashboard/**").hasRole("GESTOR")

						// Todas las demás --> rutas públicas --> Acaban en el controlador SpaFallbackController
						.anyRequest().permitAll())

				.formLogin().disable() // Desactiva la autenticación de spring
				.logout().disable() // Desactiva el logout automatico de spring
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/public/**", "/api/**") // porque aquí hay POST externos
				);

		// NOTA: No se define CSP aquí; ahora se hace dinámicamente en CspFilter
		return http.build();
	}
}
