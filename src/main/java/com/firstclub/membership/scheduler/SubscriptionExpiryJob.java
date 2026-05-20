package com.firstclub.membership.scheduler;

import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.repository.SubscriptionRepository;
import com.firstclub.membership.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Periodically transitions subscriptions whose {@code endAt} has passed into
 * the appropriate terminal state ({@code CANCELLED} if cancellation was
 * pending, otherwise {@code EXPIRED}).
 *
 * <p>The job batches candidate IDs in a quick read and then delegates each
 * transition to {@link SubscriptionService#expire(Long)} so the per-user lock
 * is acquired correctly and races with concurrent user actions are handled
 * safely (the service method is idempotent).
 */
@Component
public class SubscriptionExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryJob.class);

    private final SubscriptionRepository repository;
    private final SubscriptionService service;

    public SubscriptionExpiryJob(SubscriptionRepository repository, SubscriptionService service) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(cron = "${membership.scheduler.expiry-check-cron}")
    public void run() {
        List<SubscriptionStatus> candidates = List.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_CANCELLATION);
        List<Subscription> due = repository.findAllByStatusInAndEndAtBefore(candidates, Instant.now());
        if (due.isEmpty()) {
            return;
        }
        log.info("Expiry job processing {} subscription(s)", due.size());
        for (Subscription s : due) {
            try {
                service.expire(s.getId());
            } catch (Exception e) {
                log.warn("Failed to expire subscription {}: {}", s.getId(), e.toString());
            }
        }
    }
}
