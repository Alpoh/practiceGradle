package co.medina.starter.practice.user.service;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.repo.UserRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Either<Throwable, User> create(UserRequest request) {
        return Try.run(() -> {
            if (userRepository.existsByEmail(request.email())) {
                throw new DataIntegrityViolationException("Email already exists");
            }
        }).map(ignored -> User.builder()
                .email(request.email())
                .mobileNumber(request.mobileNumber())
                .name(request.name())
                .address(request.address())
                .build())
            .map(userRepository::save)
            .toEither();
    }

    @Override
    @Transactional(readOnly = true)
    public Either<Throwable, User> getById(Long id) {
        return Try.of(() -> userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + id)))
            .toEither();
    }

    @Override
    @Transactional(readOnly = true)
    public Either<Throwable, Page<User>> getAll(Pageable pageable) {
        return Try.of(() -> userRepository.findAll(pageable)).toEither();
    }

    @Override
    public Either<Throwable, User> update(Long id, UserRequest request) {
        // Avoid calling another @Transactional method via self (proxy would be bypassed), inline the logic
        return Try.of(() -> userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + id)))
            .toEither()
            .flatMap(existing -> Try.run(() -> {
                    if (!existing.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
                        throw new DataIntegrityViolationException("Email already exists");
                    }
                }).map(ignored -> existing).toEither()
            )
            .map(user -> {
                user.setEmail(request.email());
                user.setMobileNumber(request.mobileNumber());
                user.setName(request.name());
                user.setAddress(request.address());
                return userRepository.save(user);
            });
    }

    @Override
    public Either<Throwable, Void> delete(Long id) {
        return Try.run(() -> {
            if (!userRepository.existsById(id)) {
                throw new NoSuchElementException("User not found: " + id);
            }
            userRepository.deleteById(id);
        }).toEither();
    }
}
