package com.example.demo.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.demo.exception.DominioSyncException;
import com.example.demo.model.dto.DominioDTO;
import com.example.demo.repository.dao.CustomDominioRepository;
import com.example.demo.repository.entity.Dominio;
import com.example.demo.repository.entity.Negocio;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private CustomDominioRepository dominioRepository;

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

    // =========================================================
    // Escritura / sincronización de dominios (POST /save-domain)
    // =========================================================
    private void guardarDominioEnLaravel(Dominio dominio) {
        String endpoint = apiUrl + "/save-domain";

        Map<String, Object> payload = parseJsonToAllowedDomains(dominio);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        // TODO: Quitar debug
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(payload));
        } catch (Exception e) {
            System.out.println("Error al convertir el objeto a JSON: " + e.getMessage());
        }

        try {
            restTemplate.postForEntity(endpoint, request, String.class);
            log.info("Dominio sincronizado correctamente en Laravel: {}", dominio.getDominio());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                // Mensaje propio en español
                throw new DominioSyncException(
                        "El dominio '" + dominio.getDominio() + "' ya está registrado",
                        dominio.getDominio());
            } else {
                e.printStackTrace();
                throw new DominioSyncException(
                        "No se pudo sincronizar el dominio con la API, contacte con soporte si sigue ocurriendo el error",
                        dominio.getDominio());
            }
        } catch (RestClientException e) {
            e.printStackTrace();
            // Error de conexión, timeout, etc
            throw new DominioSyncException(
                    "No se pudo sincronizar el dominio con la API, contacte con soporte si sigue ocurriendo el error",
                    dominio.getDominio());
        }
    }

    @Override
    public void saveAll(List<DominioDTO> dominioDTOs, Negocio negocio) {
        if (dominioDTOs == null)
            return;

        for (DominioDTO dto : dominioDTOs) {
            Dominio dominio = DominioDTO.convertToEntity(dto, negocio);

            Dominio dominioExistente = dominioRepository.findByDominio(dto.getDominio());

            if (dominioExistente != null) {
                dominioRepository.delete(dominioExistente);
            }
            // Guardar primero en laravel
            guardarDominioEnLaravel(dominio);
            // Guardar en BD
            dominioRepository.save(dominio);
        }
    }

    @Override
    public void updateAll(List<DominioDTO> nuevosDTO, Negocio negocio) {
        List<Dominio> actuales = negocio.getListaDominios();

        // Identificar NUEVOS DOMINIOS (que antes no estaban)
        List<DominioDTO> nuevos = nuevosDTO.stream()
                .filter(dto -> actuales.stream().noneMatch(d -> d.getDominio().equals(dto.getDominio())))
                .toList();

        // Identificar BORRADOS (que ya no están)
        List<Dominio> borrados = actuales.stream()
                .filter(d -> nuevosDTO.stream().noneMatch(dto -> dto.getDominio().equals(d.getDominio())))
                .toList();

        // 1) Borrar de BD
        dominioRepository.deleteAll(borrados);

        // 2) Insertar nuevos (y enviarlos a Laravel)
        for (DominioDTO dto : nuevos) {
            Dominio dominio = DominioDTO.convertToEntity(dto, negocio);
            guardarDominioEnLaravel(dominio);
            dominioRepository.save(dominio);
        }
    }

    private Map<String, Object> parseJsonToAllowedDomains(Dominio dominio) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("dominio", dominio.getDominio());
        payload.put("descripcion", dominio.getDescripcion());
        payload.put("activo", dominio.isActivo());

        return payload;
    }
}
