package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Returns the subscription that is currently providing benefits for a
     * user, if any. There must be at most one ACTIVE / PENDING_CANCELLATION
     * row per user — this invariant is enforced at the service layer.
     */
    Optional<Subscription> findFirstByUserIdAndStatusInOrderByEndAtDesc(
            Long userId, List<SubscriptionStatus> statuses);

    List<Subscription> findAllByUserIdOrderByStartAtDesc(Long userId);

    List<Subscription> findAllByStatusInAndEndAtBefore(
            List<SubscriptionStatus> statuses, Instant cutoff);

    List<Subscription> findAllByStatus(SubscriptionStatus status);
}
