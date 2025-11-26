package com.example.demo.model.dto;

import java.io.Serializable;
import java.util.Objects;

import com.example.demo.model.Rol;

import lombok.Data;

@Data
public class LoginResponseDTO implements Serializable {
    private String email;
    private Rol rol;

    public LoginResponseDTO(String email, Rol rol) {
        this.email = email;
        this.rol = rol;
    }

    public LoginResponseDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LoginResponseDTO that = (LoginResponseDTO) o;
        return Objects.equals(email, that.email) && rol == that.rol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, rol);
    }
}
