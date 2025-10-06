package co.medina.starter.practice.user.service;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException("Email already exists");
        }
        User user = User.builder()
                .email(request.email())
                .mobileNumber(request.mobileNumber())
                .name(request.name())
                .address(request.address())
                .build();
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User update(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));

        if (!existing.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        existing.setEmail(request.email());
        existing.setMobileNumber(request.mobileNumber());
        existing.setName(request.name());
        existing.setAddress(request.address());
        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
