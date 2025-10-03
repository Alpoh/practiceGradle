package co.medina.starter.practice.user.api;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequest req);
}
