package com.firstclub.membership.domain.enums;

/**
 * Supported billing cycles for membership plans.
 *
 * <p>{@code durationDays} is a coarse representation used for expiry math; a
 * production system would use {@link java.time.Period} or a calendar-aware
 * date helper, but a flat day count keeps the demo simple and testable.
 */
public enum BillingCycle {
    MONTHLY(30),
    QUARTERLY(90),
    YEARLY(365);

    private final int durationDays;

    BillingCycle(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getDurationDays() {
        return durationDays;
    }
}
