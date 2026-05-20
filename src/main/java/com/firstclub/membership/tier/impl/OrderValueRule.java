package com.firstclub.membership.tier.impl;

import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.enums.CriteriaType;
import com.firstclub.membership.repository.OrderRepository;
import com.firstclub.membership.tier.TierEligibilityContext;
import com.firstclub.membership.tier.TierEligibilityRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * "User has spent at least {@code threshold} (currency) in the last
 * {@code windowDays} days." Defaults to a 30-day window — convenient for the
 * "Total Order value in a month" requirement.
 */
@Component
public class OrderValueRule implements TierEligibilityRule {

    private final OrderRepository orderRepository;

    public OrderValueRule(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public CriteriaType type() {
        return CriteriaType.ORDER_VALUE;
    }

    @Override
    public boolean isSatisfied(TierEligibilityContext context, TierEligibilityCriterion criterion) {
        if (criterion.getThreshold() == null) {
            return false;
        }
        int windowDays = criterion.getWindowDays() == null ? 30 : criterion.getWindowDays();
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        BigDecimal sum = orderRepository.sumOrderValueSince(context.user().getId(), since);
        return sum != null && sum.compareTo(criterion.getThreshold()) >= 0;
    }

    @Override
    public String describe(TierEligibilityCriterion criterion) {
        int windowDays = criterion.getWindowDays() == null ? 30 : criterion.getWindowDays();
        return "At least " + criterion.getThreshold() + " spent in last " + windowDays + " days";
    }
}
