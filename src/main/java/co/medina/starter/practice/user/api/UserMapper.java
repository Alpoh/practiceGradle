package co.medina.starter.practice.user.api;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.domain.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .name(user.getName())
                .address(user.getAddress())
                .build();
    }

    public static User toEntity(UserRequest req) {
        return User.builder()
                .email(req.getEmail())
                .mobileNumber(req.getMobileNumber())
                .name(req.getName())
                .address(req.getAddress())
                .build();
    }
}
