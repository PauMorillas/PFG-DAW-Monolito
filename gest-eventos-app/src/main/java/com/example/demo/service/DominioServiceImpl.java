package com.example.demo.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que obtiene la lista de dominios permitidos desde un API externo.
 *
 * Nota: la API Laravel devuelve un texto plano con dominios separados por
 * espacios (Content-Type: text/plain). Aquí recibimos ese String y lo
 * transformamos en una List<String> limpiando espacios en blanco.
 */
@Service
@Slf4j
public class DominioServiceImpl implements DominioService {

    public DominioServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${api.domain.url}")
    private String apiUrl;

    @Value("${api.default.domains}")
    private String defaultDomains;

    private final RestTemplate restTemplate;

    /**
     * Llama al endpoint /allowed-domains que devuelve un texto plano con dominios
     * separados por espacios. Convierte ese String en una lista y aplica un
     * fallback en caso de error o respuesta vacía.
     */
    @Override
    public List<String> findAll() {
        String endpoint = apiUrl + "/allowed-domains";

        try {
            // Pedimos el body como String (text/plain)
            String body = restTemplate.getForObject(endpoint, String.class);

            if (body == null || body.trim().isEmpty()) {
                log.warn("La respuesta llegó vacía - usando fallback a los dominios por defecto", endpoint);
                return parseDomains(defaultDomains);
            }

            // Split por cualquier whitespace (espacios, tabs, nuevas líneas), trim y
            // filtrar vacíos
            List<String> allowedDomains = Arrays.stream(body.split("\\s+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (allowedDomains.isEmpty()) {
                log.warn(
                        "Los dominios permitidos quedaron vacíos despues de parsearlos - usando fallback a los dominios por defecto",
                        endpoint);
                return parseDomains(defaultDomains);
            }

            return allowedDomains;

        } catch (RestClientException e) {
            // Manejo sencillo: log y fallback a las URLs por defecto definidas en
            // application.properties
            log.error("Error al obtener los dominios permitidos desde {}: ", endpoint, e);
            return parseDomains(defaultDomains);
        }
    }

    /**
     * Parsea una cadena de dominios separados por espacios en una lista.
     * Si la cadena es null o vacía devuelve un fallback a http://localhost:4200.
     */
    private List<String> parseDomains(String domainsPlain) {
        System.out.println("domainsPlain: " + domainsPlain);
        if (domainsPlain == null || domainsPlain.trim().isEmpty()) {
            return Arrays.asList("http://localhost:4200");
        }
        List<String> parsed = Arrays.stream(domainsPlain.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (parsed.isEmpty()) {
            return Arrays.asList("http://localhost:4200");
        }
        return parsed;
    }
}
