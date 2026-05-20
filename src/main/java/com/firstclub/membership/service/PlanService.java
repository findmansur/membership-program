package com.firstclub.membership.service;

import com.firstclub.membership.domain.dto.PlanDto;
import com.firstclub.membership.domain.entity.MembershipPlan;
import com.firstclub.membership.exception.NotFoundException;
import com.firstclub.membership.mapper.MembershipMapper;
import com.firstclub.membership.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlanService {

    private final MembershipPlanRepository repository;
    private final MembershipMapper mapper;

    public PlanService(MembershipPlanRepository repository, MembershipMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PlanDto> listActivePlans() {
        return mapper.toPlanDtos(repository.findAllByActiveTrue());
    }

    public MembershipPlan requireByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Plan not found: " + code));
    }
}
