package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.example.demo.service.ServicioService;

@Controller
public class ServicioController {

	@Autowired
	private ServicioService servicioService;

	// TODO: CRUD DE LOS SERVICIOS (DASHBOARD DE LOS CLIENTES)

//	@GetMapping("/public/reservas/{idNegocio}")
//	public ModelAndView getServicesByNegocioId(@PathVariable Long idNegocio) {
//		ModelAndView mav = new ModelAndView("services-view");
//
//		mav.addObject("serviciosDTO", servicioService.getServiciosByNegocio(idNegocio));
//		return mav;
//	}
}
