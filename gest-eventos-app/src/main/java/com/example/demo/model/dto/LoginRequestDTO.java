package com.example.demo.model.dto;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;

@Data
public class LoginRequestDTO implements Serializable {
    private String email;
    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginRequestDTO)) return false;
        LoginRequestDTO that = (LoginRequestDTO) o;
        return Objects.equals(getEmail(), that.getEmail()) &&
               Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getPassword());
    }
}
