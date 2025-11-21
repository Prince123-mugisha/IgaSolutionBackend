package com.work.IGA.Utils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    
    // Custom setter to trim whitespace
    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }
    
    // Custom setter to trim password as well
    public void setPassword(String password) {
        this.password = password != null ? password.trim() : null;
    }
}
