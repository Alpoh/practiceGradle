package co.medina.starter.practice.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    @Email
    @NotBlank
    @Size(max = 320)
    private String email;

    @Size(max = 32)
    private String mobileNumber;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String address;
}
