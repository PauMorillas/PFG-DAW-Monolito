package com.example.demo.model.dto;

import java.io.Serializable;
import java.util.Objects;

import com.example.demo.repository.entity.Dominio;
import com.example.demo.repository.entity.Negocio;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") // Para evitar el error de referencias circulares
public class DominioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String dominio;
    private String descripcion;
    private boolean activo;
    @JsonBackReference
    private NegocioDTO negocioDTO; // Guardamos solo el ID para evitar referencias circulares

    public DominioDTO() {
        this.activo = true;
    }

    public static DominioDTO convertToDTO(Dominio dominio, NegocioDTO negocioDTO) {
        DominioDTO dto = new DominioDTO();
        dto.setId(dominio.getId());
        dto.setDominio(dominio.getDominio());
        dto.setDescripcion(dominio.getDescripcion());
        dto.setActivo(dominio.isActivo());

        dto.setNegocioDTO(negocioDTO);

        return dto;
    }

    public static Dominio convertToEntity(DominioDTO dto, Negocio negocio) {
        Dominio dominio = new Dominio();
        dominio.setId(dto.getId());
        dominio.setDominio(dto.getDominio());
        dominio.setDescripcion(dto.getDescripcion());
        dominio.setActivo(dto.isActivo());

        // Asignamos la relación
        dominio.setNegocio(negocio);

        return dominio;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DominioDTO other = (DominioDTO) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
