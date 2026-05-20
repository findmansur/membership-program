package com.firstclub.membership.domain.enums;

/**
 * Identifies the configurable benefits a tier can offer.
 *
 * <p>Each value maps to a {@link com.firstclub.membership.benefit.Benefit}
 * implementation that knows how to apply itself to a cart/checkout context.
 * New benefit types can be introduced by adding a value here and a
 * matching {@code Benefit} bean — no other code needs to change.
 */
public enum BenefitType {
    FREE_DELIVERY,
    PERCENT_DISCOUNT,
    EXCLUSIVE_DEALS,
    PRIORITY_SUPPORT,
    EARLY_ACCESS
}
