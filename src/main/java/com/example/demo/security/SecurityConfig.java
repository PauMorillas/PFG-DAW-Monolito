package com.example.demo.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.repository.dao.DominioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	// 1. INYECTAR EL REPOSITORIO DE DOMINIOS
	@Autowired(required = false) // Usamos required=false por si no está implementado al inicio
	private DominioRepository dominioRepository;

	// 1. Define el encriptador de contraseñas
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 3. Define la cadena de filtros de seguridad (Autorización)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http

				.headers(headers -> headers
						// a) Deshabilitar X-Frame-Options (para que no interfiera con CSP)
						.frameOptions(frameOptions -> frameOptions.disable())

						// b) Aplicar Content-Security-Policy (CSP)
						.contentSecurityPolicy(csp -> csp
								// El método .policy() toma directamente el String de la política
								.policyDirectives(buildCspPolicy())))
				// Autorización de peticiones
				.authorizeHttpRequests(authorize -> authorize

						// 1. Rutas públicas sin autenticación (deben ir primero)
						// Incluye: Confirmación de reservas, registro, API de calendario,
						// manejo de errores de Spring y archivos estáticos.
						.requestMatchers("/public/**", "/register", "/public/api/calendario/**",
								"/public/reservas/confirmar/", "/mail/**", "/css/**",
								"/js/**", "/login", "/error", // <-- Necesario para evitar bucles de seguridad
								"/favicon.ico" // <-- Necesario para archivos de navegación
						).permitAll()

						// 2. Ruta protegida para el dashboard de gestión (gerentes)
						.requestMatchers("/", "/home", "/dashboard/**").hasRole("GESTOR")

						// 3. Permisos generales: cualquier otra petición requiere autenticación
						.anyRequest().authenticated())

				// Configuración del formulario de login
				.formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/home", true) // Redirecciona al home
																								// después del login
																								// exitoso
						.permitAll())

				// Configuración de cierre de sesión
				.logout(logout -> logout.permitAll())

				// TODO: Deshabilitar CSRF para rutas específicas (como /mail/** y /public/**)
				.csrf(csrf -> csrf.ignoringRequestMatchers("/mail/**", "/public/**"));

		return http.build();
	}

	// Método que genera la política CSP dinámicamente o estáticamente
	// TODO: El método buildCspPolicy debe ser modificado para usar el
	// dominioRepository
	private String buildCspPolicy() {

		// --- LÓGICA DE PRODUCCIÓN REAL (para tu MEMORIA FINAL) ---

		String allowedDomainsString = "";

		// Solo intentamos consultar si el repositorio ha sido inyectado
		if (dominioRepository != null) {
			// Consulta la BBDD para obtener los dominios registrados por los gerentes
			List<String> allowedDomains = dominioRepository.findAllDomains();
			// Crea un String separando los dominios por espacio
			allowedDomainsString = String.join(" ", allowedDomains);
		}

		// En caso de fallar la consulta, usamos 'self' para evitar errores de seguridad
		String finalDomains = (allowedDomainsString.isEmpty() || dominioRepository == null) ? "'self'"
				: "'self' " + allowedDomainsString;

		// La directiva frame-ancestors debe incluir 'self' MÁS los dominios de clientes
		String frameAncestorsDirective = "frame-ancestors " + finalDomains + " *;";
		// Nota: mantenemos * para la demo
		// si la lista es vacía
        
		// Resto de las directivas CSP
        String defaultSources = "default-src 'self';";
		String styleSources = "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net;";
		String scriptSources = "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net;";
		String fontSources = "font-src 'self' https://cdn.jsdelivr.net data:;";

        return frameAncestorsDirective +
               defaultSources +
               styleSources +
				scriptSources + fontSources;
	}
}
