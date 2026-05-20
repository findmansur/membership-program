package com.firstclub.membership.benefit;

import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.enums.BenefitType;

/**
 * Strategy contract for an individual benefit type.
 *
 * <p>Each implementation is keyed by a {@link BenefitType}. The runtime
 * {@link com.firstclub.membership.benefit.BenefitRegistry} dispatches to the
 * correct implementation for each {@link TierBenefit} attached to a tier.
 *
 * <p>Adding a new benefit type is a two-step change:
 * <ol>
 *   <li>Add a value to {@link BenefitType}.</li>
 *   <li>Declare a Spring bean implementing this interface.</li>
 * </ol>
 * No other code needs to change.
 */
public interface Benefit {

    /** The benefit type this strategy handles. */
    BenefitType type();

    /**
     * Apply the benefit to the given checkout context using the per-tier
     * configuration from {@link TierBenefit#getConfigJson()}.
     */
    BenefitResult apply(BenefitContext context, TierBenefit configuration);

    /**
     * Human-readable summary used when listing tier benefits (so the API can
     * describe what a tier offers without applying it to a real cart).
     */
    String summarize(TierBenefit configuration);
}
