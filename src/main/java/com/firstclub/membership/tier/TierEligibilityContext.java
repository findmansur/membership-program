package com.firstclub.membership.tier;

import com.firstclub.membership.domain.entity.User;

/**
 * Context object passed to a {@link TierEligibilityRule}. Currently just holds
 * the user, but designed to accommodate additional pre-computed signals
 * (e.g. NPS score, fraud risk) without changing every rule's signature.
 */
public record TierEligibilityContext(User user) {
}
