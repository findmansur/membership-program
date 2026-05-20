package com.firstclub.membership.domain.enums;

public enum SubscriptionStatus {
    /** Active subscription within validity window. */
    ACTIVE,
    /** User has scheduled a cancellation but is still in the paid window. */
    PENDING_CANCELLATION,
    /** Subscription cancelled before expiry; benefits are no longer applied. */
    CANCELLED,
    /** Subscription has reached its end date and was not renewed. */
    EXPIRED
}
