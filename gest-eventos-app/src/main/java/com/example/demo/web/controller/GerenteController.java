package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.service.GerenteService;

@Controller
public class GerenteController {

	@Autowired
	private GerenteService gerenteService;

	public ModelAndView getGerenteById() {

		GerenteDTO gerenteDTO = new GerenteDTO();
		ModelAndView mav = new ModelAndView();
		mav.addObject("gerenteDTO", gerenteDTO);

		return mav;
	}

	// Lógica: Sirve la página de registro con un dto vacío para rellenar con la
	// informacion del formulario
	@GetMapping("/register")
	public ModelAndView register() {
		ModelAndView mav = new ModelAndView("public/register");
		mav.addObject("gerenteDTO", new GerenteDTO());
		return mav;
	}

	// Lógica: Procesa el formulario y registra el nuevo Gerente.
	@PostMapping("/register")
	public ModelAndView registerUser(@ModelAttribute("gerenteDTO") GerenteDTO gerenteDTO) {
		ModelAndView mav = new ModelAndView();
		String nombreVista = "";
		try {
			// Llama al servicio para manejar la lógica de negocio (encriptación y guardado)
			gerenteService.save(gerenteDTO);

			// Redirigir al login con un parámetro de éxito
			nombreVista = "redirect:/login?registered";

		} catch (IllegalStateException error) {
			// Manejo de errores:
			mav.addObject("gerenteDTO", gerenteDTO); // Mantenemos los datos introducidos
			mav.addObject("error", error.getMessage()); // Mensaje de error personalizado del service
			nombreVista = "register"; // Devolver al registro
		}

		mav.setViewName(nombreVista);
		return mav;
	}

}
