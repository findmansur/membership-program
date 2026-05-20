package com.firstclub.membership.tier;

import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.enums.CriteriaType;

/**
 * Strategy for evaluating a single {@link TierEligibilityCriterion}. One rule
 * per {@link CriteriaType}. The rule engine ({@link TierEligibilityEvaluator})
 * composes rule results using each criterion's {@code required} flag.
 */
public interface TierEligibilityRule {

    CriteriaType type();

    boolean isSatisfied(TierEligibilityContext context, TierEligibilityCriterion criterion);

    /** Used in API responses so clients can show "why" a tier is/isn't unlocked. */
    String describe(TierEligibilityCriterion criterion);
}
