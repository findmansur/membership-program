package com.firstclub.membership.tier.impl;

import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.enums.CriteriaType;
import com.firstclub.membership.repository.OrderRepository;
import com.firstclub.membership.tier.TierEligibilityContext;
import com.firstclub.membership.tier.TierEligibilityRule;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * "User has placed at least {@code threshold} orders in the last
 * {@code windowDays} days." Defaults to a 30-day window when not specified.
 */
@Component
public class OrderCountRule implements TierEligibilityRule {

    private final OrderRepository orderRepository;

    public OrderCountRule(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public CriteriaType type() {
        return CriteriaType.ORDER_COUNT;
    }

    @Override
    public boolean isSatisfied(TierEligibilityContext context, TierEligibilityCriterion criterion) {
        if (criterion.getThreshold() == null) {
            return false;
        }
        int windowDays = criterion.getWindowDays() == null ? 30 : criterion.getWindowDays();
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        long count = orderRepository.countOrdersSince(context.user().getId(), since);
        return count >= criterion.getThreshold().longValue();
    }

    @Override
    public String describe(TierEligibilityCriterion criterion) {
        int windowDays = criterion.getWindowDays() == null ? 30 : criterion.getWindowDays();
        return "At least " + criterion.getThreshold() + " orders in last " + windowDays + " days";
    }
}
