package com.firstclub.membership.controller;

import com.firstclub.membership.domain.dto.PlanDto;
import com.firstclub.membership.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<PlanDto> listPlans() {
        return planService.listActivePlans();
    }
}
