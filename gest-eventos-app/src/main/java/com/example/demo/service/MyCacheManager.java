package com.example.demo.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MyCacheManager {
    public static final String RESERVAS_CACHE = "reservasPorServicio";
    public static final String DOMAINS_CACHE = "allowedDomains";

    @CacheEvict(value = RESERVAS_CACHE, key = "#idServicio")
    public void invalidateReservasCache(Long idServicio) {
        log.info("Cache de reservas para el servicio ID {} invalidada.", idServicio);
    }

    @CacheEvict(value = DOMAINS_CACHE, allEntries = true)
    public void invalidateAllowedDomainsCache() {
        log.info("Cache de dominios permitidos invalidado globalmente.");
    }
}
