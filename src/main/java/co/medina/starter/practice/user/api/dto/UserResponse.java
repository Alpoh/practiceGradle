package co.medina.starter.practice.user.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {
    Long id;
    String email;
    String mobileNumber;
    String name;
    String address;
}
