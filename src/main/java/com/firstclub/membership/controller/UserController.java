package com.firstclub.membership.controller;

import com.firstclub.membership.domain.dto.CreateUserRequest;
import com.firstclub.membership.domain.dto.UpdateUserCohortRequest;
import com.firstclub.membership.domain.dto.UserDto;
import com.firstclub.membership.mapper.MembershipMapper;
import com.firstclub.membership.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final MembershipMapper mapper;

    public UserController(UserService userService, MembershipMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toUserDto(userService.create(request)));
    }

    @GetMapping
    public List<UserDto> list() {
        return mapper.toUserDtos(userService.list());
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return mapper.toUserDto(userService.requireById(id));
    }

    @PatchMapping("/{id}/cohort")
    public UserDto updateCohort(@PathVariable Long id, @Valid @RequestBody UpdateUserCohortRequest request) {
        return mapper.toUserDto(userService.updateCohort(id, request.cohort()));
    }
}
