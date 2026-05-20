package com.firstclub.membership.service;

import com.firstclub.membership.domain.dto.CreateUserRequest;
import com.firstclub.membership.domain.entity.User;
import com.firstclub.membership.exception.NotFoundException;
import com.firstclub.membership.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public User create(CreateUserRequest req) {
        return repository.save(new User(req.name(), req.email(), req.cohort()));
    }

    @Transactional(readOnly = true)
    public User requireById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return repository.findAll();
    }

    @Transactional
    public User updateCohort(Long userId, String cohort) {
        User user = requireById(userId);
        user.setCohort(cohort);
        return repository.save(user);
    }
}
