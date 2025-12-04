package com.example.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FallbackController {

    // Captura todas las rutas que no sean API ni public
    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/public/demo";
    }
}
