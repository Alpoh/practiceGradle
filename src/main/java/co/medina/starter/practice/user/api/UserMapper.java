package co.medina.starter.practice.user.api;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    User toEntity(UserRequest req);
}
