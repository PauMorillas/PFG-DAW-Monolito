package com.example.demo.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Lazy;

import com.example.demo.service.DominioService;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Value("${api.secret}")
	private final String apiToken = null;

	private final DominioService dominioService;

	public SecurityConfig(@Lazy DominioService dominioService) {
		this.dominioService = dominioService;
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		try {

			restTemplate.getInterceptors().add((request, body, execution) -> {
				request.getHeaders().set("Authorization", "Bearer " + apiToken);
				request.getHeaders().set("Accept", "text/plain");
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
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

	// 3. Define la cadena de filtros de seguridad (Autorización)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors() // Usará automáticamente el CorsConfigurationSource del CorsConfig
				.and()
				.headers(headers -> headers
						.frameOptions(frameOptions -> frameOptions.disable()) // No bloquear iframes
				)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/api/clientes/registro",
								"/public/**",
								"/register",
								"/public/api/calendario/**",
								"/public/reservas/confirmar/",
								"/mail/**",
								"/css/**",
								"/js/**",
								"/login",
								"/error",
								"/favicon.ico")
						.permitAll()
						.requestMatchers("/", "/home", "/dashboard/**").hasRole("GESTOR")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/home", true)
						.permitAll())
				.logout(logout -> logout.permitAll())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/api/clientes/registro", "/mail/**", "/public/**"));

		// NOTA: No se define CSP aquí; ahora se hace dinámicamente en CspFilter

		return http.build();
	}
}
