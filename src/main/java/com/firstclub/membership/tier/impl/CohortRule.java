package com.firstclub.membership.tier.impl;

import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.enums.CriteriaType;
import com.firstclub.membership.tier.TierEligibilityContext;
import com.firstclub.membership.tier.TierEligibilityRule;
import org.springframework.stereotype.Component;

/**
 * Matches users belonging to a specific cohort label (e.g. "VIP", "EMPLOYEE").
 * Useful for granting exclusive tiers to curated user segments without having
 * to encode them in business code.
 */
@Component
public class CohortRule implements TierEligibilityRule {

    @Override
    public CriteriaType type() {
        return CriteriaType.COHORT;
    }

    @Override
    public boolean isSatisfied(TierEligibilityContext context, TierEligibilityCriterion criterion) {
        String required = criterion.getStringValue();
        if (required == null || required.isBlank()) {
            return false;
        }
        String userCohort = context.user().getCohort();
        return required.equalsIgnoreCase(userCohort);
    }

    @Override
    public String describe(TierEligibilityCriterion criterion) {
        return "User belongs to cohort: " + criterion.getStringValue();
    }
}
