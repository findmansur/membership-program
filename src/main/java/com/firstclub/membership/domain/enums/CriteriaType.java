package com.firstclub.membership.domain.enums;

/**
 * Criteria used to evaluate whether a user is eligible for a given tier.
 *
 * <p>The {@link com.firstclub.membership.tier.TierEligibilityRule} indexed by
 * each value defines the actual evaluation logic, so new criteria can be added
 * by introducing a new enum constant and a matching rule bean.
 */
public enum CriteriaType {
    /** User must have placed at least N orders in the lookback window. */
    ORDER_COUNT,
    /** User's total order value must be at least X in the lookback window. */
    ORDER_VALUE,
    /** User must belong to a specific cohort label (e.g. EMPLOYEE, VIP). */
    COHORT
}
