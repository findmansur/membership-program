package com.firstclub.membership.service;

import com.firstclub.membership.domain.dto.CreateOrderRequest;
import com.firstclub.membership.domain.entity.OrderRecord;
import com.firstclub.membership.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final UserService userService;

    public OrderService(OrderRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Transactional
    public OrderRecord create(CreateOrderRequest req) {
        // Ensure user exists so we don't accumulate orders for ghost users.
        userService.requireById(req.userId());
        return repository.save(new OrderRecord(req.userId(), req.amount(), Instant.now()));
    }
}
