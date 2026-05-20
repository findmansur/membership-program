package com.firstclub.membership.controller;

import com.firstclub.membership.domain.dto.TierDto;
import com.firstclub.membership.domain.dto.TierEligibilityDto;
import com.firstclub.membership.service.TierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tiers")
public class TierController {

    private final TierService tierService;

    public TierController(TierService tierService) {
        this.tierService = tierService;
    }

    @GetMapping
    public List<TierDto> listTiers() {
        return tierService.listActiveTiers();
    }

    @GetMapping("/eligibility/{userId}")
    public List<TierEligibilityDto> eligibility(@PathVariable Long userId) {
        return tierService.getEligibleTiersForUser(userId);
    }
}
