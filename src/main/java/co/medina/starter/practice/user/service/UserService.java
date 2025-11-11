package co.medina.starter.practice.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.domain.User;
import io.vavr.control.Either;

public interface UserService {
    Either<Throwable, User> create(UserRequest request);
    Either<Throwable, User> getById(Long id);
    Either<Throwable, Page<User>> getAll(Pageable pageable);
    Either<Throwable, User> update(Long id, UserRequest request);
    Either<Throwable, Void> delete(Long id);
}
