package com.firstclub.membership.scheduler;

import com.firstclub.membership.domain.entity.MembershipTier;
import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.SubscriptionRepository;
import com.firstclub.membership.repository.UserRepository;
import com.firstclub.membership.service.SubscriptionService;
import com.firstclub.membership.tier.TierEligibilityContext;
import com.firstclub.membership.tier.TierEligibilityEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Periodically re-evaluates each active subscriber's tier eligibility and
 * auto-upgrades them to the highest tier they currently qualify for.
 *
 * <p>Auto-downgrade is intentionally not performed by this job — users keep
 * the benefits they paid for until their subscription expires or they
 * explicitly downgrade. If business rules ever require automatic downgrades
 * (e.g. fraud holds), they should be expressed via an explicit
 * "downgrade-reason" path, not by silently demoting users here.
 */
@Component
public class TierReevaluationJob {

    private static final Logger log = LoggerFactory.getLogger(TierReevaluationJob.class);

    private final SubscriptionRepository subscriptionRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final TierEligibilityEvaluator evaluator;

    @Value("${membership.tier.auto-upgrade-enabled:true}")
    private boolean autoUpgradeEnabled;

    public TierReevaluationJob(SubscriptionRepository subscriptionRepository,
                               MembershipTierRepository tierRepository,
                               UserRepository userRepository,
                               SubscriptionService subscriptionService,
                               TierEligibilityEvaluator evaluator) {
        this.subscriptionRepository = subscriptionRepository;
        this.tierRepository = tierRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.evaluator = evaluator;
    }

    @Scheduled(cron = "${membership.scheduler.tier-reevaluation-cron}")
    public void run() {
        if (!autoUpgradeEnabled) {
            return;
        }
        List<MembershipTier> tiersByRankDesc = tierRepository.findAllByActiveTrueOrderByRankAsc()
                .stream()
                .sorted(Comparator.comparingInt(MembershipTier::getRank).reversed())
                .toList();

        List<Subscription> active = subscriptionRepository.findAllByStatus(SubscriptionStatus.ACTIVE);
        if (active.isEmpty()) return;

        log.info("Tier re-evaluation reviewing {} active subscription(s)", active.size());

        for (Subscription sub : active) {
            try {
                Optional<MembershipTier> best = userRepository.findById(sub.getUserId())
                        .map(TierEligibilityContext::new)
                        .flatMap(ctx -> tiersByRankDesc.stream()
                                .filter(t -> evaluator.evaluate(t, ctx).eligible())
                                .findFirst());

                if (best.isPresent() && best.get().getRank() > sub.getTier().getRank()) {
                    subscriptionService.changeTier(sub.getId(), best.get().getCode());
                    log.info("Auto-upgraded subscription {} from {} to {}",
                            sub.getId(), sub.getTier().getCode(), best.get().getCode());
                }
            } catch (Exception e) {
                log.warn("Auto-upgrade failed for subscription {}: {}", sub.getId(), e.toString());
            }
        }
    }
}
