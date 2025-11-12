package com.example.demo.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.DominioService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CspFilter extends OncePerRequestFilter {

    private final DominioService dominioService;

    public CspFilter(DominioService dominioService) {
        this.dominioService = dominioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Obtiene los dominios actuales desde Laravel
        List<String> allowedDomains = dominioService.findAll();
        String allowedDomainsString = String.join(" ", allowedDomains);

        // Construye la directiva frame-ancestors
        String frameAncestors = (allowedDomainsString.isEmpty() ? "'self'" : "'self' " + allowedDomainsString);

        // Construye la CSP completa
        String csp = "frame-ancestors " + frameAncestors + " *;" +
                     "default-src 'self';" +
                     "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net;" +
                     "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net;" +
                     "font-src 'self' https://cdn.jsdelivr.net data:;";
        log.info("Políticas generadas: {}", csp);

        // Añade la cabecera CSP a la respuesta
        response.setHeader("Content-Security-Policy", csp);

        // Continúa con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
