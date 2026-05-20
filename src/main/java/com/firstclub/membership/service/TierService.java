package com.firstclub.membership.service;

import com.firstclub.membership.benefit.Benefit;
import com.firstclub.membership.benefit.BenefitRegistry;
import com.firstclub.membership.domain.dto.TierBenefitDto;
import com.firstclub.membership.domain.dto.TierDto;
import com.firstclub.membership.domain.dto.TierEligibilityDto;
import com.firstclub.membership.domain.entity.MembershipTier;
import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.entity.User;
import com.firstclub.membership.exception.NotFoundException;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.UserRepository;
import com.firstclub.membership.tier.TierEligibilityContext;
import com.firstclub.membership.tier.TierEligibilityEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TierService {

    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;
    private final BenefitRegistry benefitRegistry;
    private final TierEligibilityEvaluator evaluator;

    public TierService(MembershipTierRepository tierRepository,
                       UserRepository userRepository,
                       BenefitRegistry benefitRegistry,
                       TierEligibilityEvaluator evaluator) {
        this.tierRepository = tierRepository;
        this.userRepository = userRepository;
        this.benefitRegistry = benefitRegistry;
        this.evaluator = evaluator;
    }

    public List<TierDto> listActiveTiers() {
        return tierRepository.findAllByActiveTrueOrderByRankAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public MembershipTier requireByCode(String code) {
        return tierRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Tier not found: " + code));
    }

    public List<TierEligibilityDto> getEligibleTiersForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        TierEligibilityContext ctx = new TierEligibilityContext(user);
        return tierRepository.findAllByActiveTrueOrderByRankAsc().stream()
                .map(tier -> {
                    var decision = evaluator.evaluate(tier, ctx);
                    return new TierEligibilityDto(
                            tier.getCode(),
                            tier.getName(),
                            tier.getRank(),
                            decision.eligible(),
                            decision.notes());
                })
                .toList();
    }

    private TierDto toDto(MembershipTier tier) {
        List<TierBenefitDto> benefits = tier.getBenefits().stream()
                .filter(TierBenefit::isActive)
                .map(b -> {
                    Benefit handler = benefitRegistry.forType(b.getType());
                    return new TierBenefitDto(b.getType(), b.getDescription(), handler.summarize(b));
                })
                .toList();
        List<String> criteria = tier.getEligibilityCriteria().stream()
                .map(this::describeCriterion)
                .toList();
        return new TierDto(
                tier.getId(),
                tier.getCode(),
                tier.getName(),
                tier.getDescription(),
                tier.getRank(),
                benefits,
                criteria);
    }

    private String describeCriterion(TierEligibilityCriterion c) {
        String suffix = c.isRequired() ? " [required]" : " [optional]";
        return switch (c.getType()) {
            case ORDER_COUNT -> "At least " + c.getThreshold() + " orders in last "
                    + (c.getWindowDays() == null ? 30 : c.getWindowDays()) + " days" + suffix;
            case ORDER_VALUE -> "At least " + c.getThreshold() + " spent in last "
                    + (c.getWindowDays() == null ? 30 : c.getWindowDays()) + " days" + suffix;
            case COHORT -> "User cohort = " + c.getStringValue() + suffix;
        };
    }
}
