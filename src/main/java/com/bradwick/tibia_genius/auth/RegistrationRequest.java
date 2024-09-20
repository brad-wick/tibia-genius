package com.bradwick.tibia_genius.auth;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotBlank(message = "The email must not be empty")
    @Email(message = "The email must be in a valid format")
    private String email;

    @NotBlank(message = "The password must not be empty")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters long")
    @Pattern(regexp = ".*[A-Za-z].*", message = "Password must contain at least one letter")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    @Pattern(regexp = ".*[!@#$%^&*()].*", message = "Password must contain at least one special character")
    private String password;
}
