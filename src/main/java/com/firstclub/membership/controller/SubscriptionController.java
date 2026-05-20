package com.firstclub.membership.controller;

import com.firstclub.membership.domain.dto.ChangeTierRequest;
import com.firstclub.membership.domain.dto.SubscribeRequest;
import com.firstclub.membership.domain.dto.SubscriptionDto;
import com.firstclub.membership.exception.NotFoundException;
import com.firstclub.membership.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscribeRequest request) {
        var sub = service.subscribe(request.userId(), request.planCode(), request.tierCode(), request.autoRenew());
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionDto.from(sub));
    }

    @GetMapping("/user/{userId}/current")
    public SubscriptionDto current(@PathVariable Long userId) {
        return service.findCurrent(userId)
                .map(SubscriptionDto::from)
                .orElseThrow(() -> new NotFoundException("No active subscription for user " + userId));
    }

    @GetMapping("/user/{userId}")
    public List<SubscriptionDto> history(@PathVariable Long userId) {
        return service.history(userId).stream().map(SubscriptionDto::from).toList();
    }

    @PostMapping("/{id}/upgrade")
    public SubscriptionDto upgrade(@PathVariable Long id, @Valid @RequestBody ChangeTierRequest request) {
        return SubscriptionDto.from(service.changeTier(id, request.tierCode()));
    }

    @PostMapping("/{id}/downgrade")
    public SubscriptionDto downgrade(@PathVariable Long id, @Valid @RequestBody ChangeTierRequest request) {
        return SubscriptionDto.from(service.changeTier(id, request.tierCode()));
    }

    @PostMapping("/{id}/cancel")
    public SubscriptionDto cancel(@PathVariable Long id,
                                  @RequestParam(defaultValue = "false") boolean immediate) {
        return SubscriptionDto.from(service.cancel(id, immediate));
    }

    @PostMapping("/{id}/renew")
    public SubscriptionDto renew(@PathVariable Long id) {
        return SubscriptionDto.from(service.renew(id));
    }
}
