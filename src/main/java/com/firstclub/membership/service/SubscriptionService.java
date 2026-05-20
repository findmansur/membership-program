package com.firstclub.membership.service;

import com.firstclub.membership.concurrency.KeyedLock;
import com.firstclub.membership.domain.entity.MembershipPlan;
import com.firstclub.membership.domain.entity.MembershipTier;
import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.entity.SubscriptionEvent;
import com.firstclub.membership.domain.entity.User;
import com.firstclub.membership.domain.enums.SubscriptionEventType;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.exception.BadRequestException;
import com.firstclub.membership.exception.ConflictException;
import com.firstclub.membership.exception.NotFoundException;
import com.firstclub.membership.repository.SubscriptionEventRepository;
import com.firstclub.membership.repository.SubscriptionRepository;
import com.firstclub.membership.tier.TierEligibilityContext;
import com.firstclub.membership.tier.TierEligibilityEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Orchestrates the lifecycle of subscriptions.
 *
 * <p><b>Invariants:</b>
 * <ul>
 *   <li>A user has at most one subscription in {@code ACTIVE} or
 *       {@code PENDING_CANCELLATION} state at any time.</li>
 *   <li>State transitions are append-only to {@code subscription_events}
 *       for traceability.</li>
 * </ul>
 *
 * <p><b>Concurrency strategy:</b>
 * <ol>
 *   <li>All mutating operations acquire a per-user lock via {@link KeyedLock}
 *       so concurrent requests on the same user are serialised. Different
 *       users still run in parallel.</li>
 *   <li>The lock is taken <em>around</em> the transaction (not inside) so the
 *       transaction commits and releases its row locks before the next waiter
 *       reads the latest state.</li>
 *   <li>The {@code @Version} field on {@link Subscription} provides a second
 *       line of defence against stale background writers — the framework
 *       throws {@code OptimisticLockingFailureException} on conflict, which
 *       the global handler maps to a 409.</li>
 * </ol>
 *
 * <p>The transactional methods are invoked through a {@link TransactionTemplate}
 * so a new transaction is started inside the lock without needing a separate
 * bean (and without falling foul of Spring's self-invocation proxy limitation).
 */
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private static final List<SubscriptionStatus> CURRENT_STATUSES = List.of(
            SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_CANCELLATION);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEventRepository eventRepository;
    private final PlanService planService;
    private final TierService tierService;
    private final UserService userService;
    private final TierEligibilityEvaluator eligibilityEvaluator;
    private final KeyedLock<Long> userLock;
    private final TransactionTemplate txTemplate;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SubscriptionEventRepository eventRepository,
                               PlanService planService,
                               TierService tierService,
                               UserService userService,
                               TierEligibilityEvaluator eligibilityEvaluator,
                               KeyedLock<Long> userLock,
                               PlatformTransactionManager txManager) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventRepository = eventRepository;
        this.planService = planService;
        this.tierService = tierService;
        this.userService = userService;
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.userLock = userLock;
        this.txTemplate = new TransactionTemplate(txManager);
    }

    // --------------- Reads ---------------

    @Transactional(readOnly = true)
    public Optional<Subscription> findCurrent(Long userId) {
        return subscriptionRepository
                .findFirstByUserIdAndStatusInOrderByEndAtDesc(userId, CURRENT_STATUSES)
                .filter(s -> s.isCurrentlyActive(Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<Subscription> history(Long userId) {
        return subscriptionRepository.findAllByUserIdOrderByStartAtDesc(userId);
    }

    // --------------- Mutations ---------------

    public Subscription subscribe(Long userId, String planCode, String tierCode, boolean autoRenew) {
        return runLockedAndTransactional(userId, () -> {
            User user = userService.requireById(userId);
            if (findCurrentInternal(userId).isPresent()) {
                throw new ConflictException("User " + userId + " already has an active subscription.");
            }
            MembershipPlan plan = planService.requireByCode(planCode);
            MembershipTier tier = tierService.requireByCode(tierCode);
            ensureUserEligibleForTier(user, tier);

            Instant now = Instant.now();
            Instant endAt = now.plus(plan.getBillingCycle().getDurationDays(), ChronoUnit.DAYS);
            Subscription subscription = new Subscription(userId, plan, tier, now, endAt);
            subscription.setAutoRenew(autoRenew);
            subscription = subscriptionRepository.save(subscription);

            recordEvent(subscription, SubscriptionEventType.SUBSCRIBED, null, tier.getCode(),
                    "Subscribed to " + plan.getCode() + " / " + tier.getCode());
            log.info("User {} subscribed to plan={} tier={} (id={})",
                    userId, planCode, tierCode, subscription.getId());
            return subscription;
        });
    }

    public Subscription changeTier(Long subscriptionId, String newTierCode) {
        Long userId = lookupUserId(subscriptionId);
        return runLockedAndTransactional(userId, () -> {
            Subscription sub = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));
            if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
                throw new BadRequestException("Tier can only be changed on an ACTIVE subscription. " +
                        "Current status: " + sub.getStatus());
            }
            MembershipTier newTier = tierService.requireByCode(newTierCode);
            MembershipTier oldTier = sub.getTier();
            if (oldTier.getId().equals(newTier.getId())) {
                throw new BadRequestException("Subscription already on tier " + newTier.getCode());
            }
            User user = userService.requireById(sub.getUserId());
            ensureUserEligibleForTier(user, newTier);

            SubscriptionEventType eventType = newTier.getRank() > oldTier.getRank()
                    ? SubscriptionEventType.UPGRADED
                    : SubscriptionEventType.DOWNGRADED;
            sub.setTier(newTier);
            sub = subscriptionRepository.save(sub);

            recordEvent(sub, eventType, oldTier.getCode(), newTier.getCode(),
                    "Tier changed from " + oldTier.getCode() + " to " + newTier.getCode());
            log.info("Subscription {} {} from tier {} to {}",
                    sub.getId(), eventType, oldTier.getCode(), newTier.getCode());
            return sub;
        });
    }

    public Subscription cancel(Long subscriptionId, boolean immediate) {
        Long userId = lookupUserId(subscriptionId);
        return runLockedAndTransactional(userId, () -> {
            Subscription sub = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));
            if (sub.getStatus() == SubscriptionStatus.CANCELLED || sub.getStatus() == SubscriptionStatus.EXPIRED) {
                throw new BadRequestException("Subscription is already " + sub.getStatus());
            }
            sub.setAutoRenew(false);
            if (immediate) {
                sub.setStatus(SubscriptionStatus.CANCELLED);
                sub.setEndAt(Instant.now());
                sub = subscriptionRepository.save(sub);
                recordEvent(sub, SubscriptionEventType.CANCELLED, sub.getTier().getCode(), null,
                        "Immediate cancellation by user request");
            } else {
                sub.setStatus(SubscriptionStatus.PENDING_CANCELLATION);
                sub = subscriptionRepository.save(sub);
                recordEvent(sub, SubscriptionEventType.CANCELLATION_REQUESTED,
                        sub.getTier().getCode(), null,
                        "Cancellation scheduled; benefits remain until " + sub.getEndAt());
            }
            log.info("Subscription {} cancellation: immediate={}", sub.getId(), immediate);
            return sub;
        });
    }

    public Subscription renew(Long subscriptionId) {
        Long userId = lookupUserId(subscriptionId);
        return runLockedAndTransactional(userId, () -> {
            Subscription sub = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));
            if (sub.getStatus() != SubscriptionStatus.ACTIVE && sub.getStatus() != SubscriptionStatus.EXPIRED) {
                throw new BadRequestException("Cannot renew a subscription in status " + sub.getStatus());
            }
            Instant base = sub.getEndAt().isBefore(Instant.now()) ? Instant.now() : sub.getEndAt();
            Instant newEnd = base.plus(sub.getPlan().getBillingCycle().getDurationDays(), ChronoUnit.DAYS);
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setEndAt(newEnd);
            sub = subscriptionRepository.save(sub);
            recordEvent(sub, SubscriptionEventType.RENEWED, sub.getTier().getCode(), sub.getTier().getCode(),
                    "Renewed until " + newEnd);
            log.info("Subscription {} renewed until {}", sub.getId(), newEnd);
            return sub;
        });
    }

    /**
     * Invoked by the expiry scheduler. Idempotent — if a concurrent user
     * action has already moved the subscription out of a current state, this
     * method silently returns.
     */
    public void expire(Long subscriptionId) {
        Long userId;
        try {
            userId = lookupUserId(subscriptionId);
        } catch (NotFoundException e) {
            return;
        }
        runLockedAndTransactional(userId, () -> {
            Subscription sub = subscriptionRepository.findById(subscriptionId).orElse(null);
            if (sub == null) return null;
            if (sub.getStatus() != SubscriptionStatus.ACTIVE
                    && sub.getStatus() != SubscriptionStatus.PENDING_CANCELLATION) {
                return null;
            }
            if (sub.getEndAt().isAfter(Instant.now())) {
                return null;
            }
            SubscriptionStatus newStatus = sub.getStatus() == SubscriptionStatus.PENDING_CANCELLATION
                    ? SubscriptionStatus.CANCELLED
                    : SubscriptionStatus.EXPIRED;
            sub.setStatus(newStatus);
            subscriptionRepository.save(sub);
            recordEvent(sub,
                    newStatus == SubscriptionStatus.CANCELLED
                            ? SubscriptionEventType.CANCELLED
                            : SubscriptionEventType.EXPIRED,
                    sub.getTier().getCode(), null,
                    "Auto-transition by scheduler at " + Instant.now());
            log.info("Subscription {} auto-transitioned to {}", sub.getId(), newStatus);
            return null;
        });
    }

    // --------------- Helpers ---------------

    private <T> T runLockedAndTransactional(Long userId, Supplier<T> body) {
        return userLock.executeLocked(userId, () -> txTemplate.execute(status -> body.get()));
    }

    private Long lookupUserId(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(Subscription::getUserId)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));
    }

    private Optional<Subscription> findCurrentInternal(Long userId) {
        return subscriptionRepository
                .findFirstByUserIdAndStatusInOrderByEndAtDesc(userId, CURRENT_STATUSES)
                .filter(s -> s.isCurrentlyActive(Instant.now()));
    }

    private void recordEvent(Subscription sub, SubscriptionEventType type,
                             String fromTier, String toTier, String note) {
        eventRepository.save(new SubscriptionEvent(sub, type, fromTier, toTier, note, Instant.now()));
    }

    private void ensureUserEligibleForTier(User user, MembershipTier tier) {
        var decision = eligibilityEvaluator.evaluate(tier, new TierEligibilityContext(user));
        if (!decision.eligible()) {
            throw new BadRequestException(
                    "User is not eligible for tier " + tier.getCode() + ". Requirements: " + decision.notes());
        }
    }
}
