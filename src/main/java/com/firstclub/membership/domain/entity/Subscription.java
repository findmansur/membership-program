package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A user's active or historical subscription to a plan + tier.
 *
 * <p>Concurrency model:
 * <ul>
 *   <li>{@link #version} field gives optimistic locking — concurrent mutations
 *       on the same row will fail with {@code OptimisticLockingFailureException}
 *       so callers can retry without holding row locks.</li>
 *   <li>The service layer additionally serializes operations per user via a
 *       keyed lock so we never end up with two concurrent active subscriptions
 *       for the same user.</li>
 * </ul>
 */
@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subs_user_status", columnList = "user_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SubscriptionStatus status;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = false;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Subscription(Long userId, MembershipPlan plan, MembershipTier tier,
                        Instant startAt, Instant endAt) {
        this.userId = userId;
        this.plan = plan;
        this.tier = tier;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = SubscriptionStatus.ACTIVE;
    }

    /**
     * @return true if the subscription is currently providing benefits — i.e.
     *         status is ACTIVE or PENDING_CANCELLATION and we are still inside
     *         the validity window.
     */
    public boolean isCurrentlyActive(Instant now) {
        return (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.PENDING_CANCELLATION)
                && !now.isBefore(startAt)
                && now.isBefore(endAt);
    }
}
