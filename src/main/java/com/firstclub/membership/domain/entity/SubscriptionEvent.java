package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.SubscriptionEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Append-only audit record for any change made to a subscription.
 *
 * <p>Keeping a separate event log (rather than mutating the subscription in
 * place) preserves history, helps with debugging concurrency issues, and
 * gives us the building blocks for analytics later (churn, upgrade funnels,
 * etc.) without touching the live row.
 */
@Entity
@Table(name = "subscription_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private SubscriptionEventType eventType;

    @Column(name = "from_tier", length = 64)
    private String fromTier;

    @Column(name = "to_tier", length = 64)
    private String toTier;

    @Column(length = 512)
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public SubscriptionEvent(Subscription subscription, SubscriptionEventType eventType,
                             String fromTier, String toTier, String note, Instant occurredAt) {
        this.subscription = subscription;
        this.eventType = eventType;
        this.fromTier = fromTier;
        this.toTier = toTier;
        this.note = note;
        this.occurredAt = occurredAt;
    }
}
