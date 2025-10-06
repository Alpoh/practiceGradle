package co.medina.starter.practice.user.service;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    User create(UserRequest request);
    Optional<User> getById(Long id);
    Page<User> getAll(Pageable pageable);
    User update(Long id, UserRequest request);
    void delete(Long id);
}
