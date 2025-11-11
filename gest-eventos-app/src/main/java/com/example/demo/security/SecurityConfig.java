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

import com.example.demo.service.DominioService;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Value("${api.secret}")
    private final String apiToken = null;

	private final DominioService dominioService = null;

	@Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + apiToken);
            return execution.execute(request, body);
        });
        return restTemplate;
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
                .frameOptions(frameOptions -> frameOptions.disable())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(buildCspPolicy())))
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
                    "/favicon.ico"
                ).permitAll()
                .requestMatchers("/", "/home", "/dashboard/**").hasRole("GESTOR")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll())
            .logout(logout -> logout.permitAll())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/clientes/registro", "/mail/**", "/public/**"));

				log.info("Políticas creadas: " + buildCspPolicy());
        return http.build();
    }

	// Método que genera la política CSP dinámicamente
	private String buildCspPolicy() {

		String allowedDomainsString = "";

		// Solo intentamos consultar si el repositorio ha sido inyectado
		if (dominioService != null) {
			// Consulta la BBDD para obtener los dominios registrados por los gerentes
			List<String> allowedDomains = dominioService.findAll();
			// Crea un String separando los dominios por espacio
			allowedDomainsString = String.join(" ", allowedDomains);
		}

		log.info("Dominios permitidos: {}", allowedDomainsString);

		// En caso de fallar la consulta, usamos 'self' para evitar errores de seguridad
		String finalDomains = (allowedDomainsString.isEmpty() || dominioService == null) ? "'self'"
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
