package co.medina.starter.practice.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRequest(
        @Email @NotBlank @Size(max = 320) String email,
        @Size(max = 32) String mobileNumber,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String address
) {}
