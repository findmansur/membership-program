package com.firstclub.membership.service;

import com.firstclub.membership.domain.dto.CreateOrderRequest;
import com.firstclub.membership.domain.entity.OrderRecord;
import com.firstclub.membership.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
        userService.requireById(req.userId());
        return repository.save(new OrderRecord(req.userId(), req.amount(), Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<OrderRecord> getUserOrders(Long userId) {
        userService.requireById(userId);
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}
