package com.example.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	// Muestra la vita del
	@GetMapping("/login")
	public String showLoginForm() {
		return "public/login"; // Retorna el nombre de la plantilla login.html
	}

	// El resto de la lógica de autenticación (el POST a /login) es invisible
	// y lo maneja Spring Security automáticamente.
}
