package com.firstclub.membership.controller;

import com.firstclub.membership.domain.dto.CreateOrderRequest;
import com.firstclub.membership.domain.dto.OrderDto;
import com.firstclub.membership.mapper.MembershipMapper;
import com.firstclub.membership.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final MembershipMapper mapper;

    public OrderController(OrderService orderService, MembershipMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toOrderDto(orderService.create(request)));
    }

    @GetMapping("/user/{userId}")
    public List<OrderDto> getUserOrders(@PathVariable Long userId) {
        return mapper.toOrderDtos(orderService.getUserOrders(userId));
    }
}
