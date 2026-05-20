package com.firstclub.membership.controller;

import com.firstclub.membership.domain.dto.CheckoutPreviewRequest;
import com.firstclub.membership.domain.dto.CheckoutPreviewResponse;
import com.firstclub.membership.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/preview")
    public CheckoutPreviewResponse preview(@Valid @RequestBody CheckoutPreviewRequest request) {
        return checkoutService.preview(request);
    }
}
