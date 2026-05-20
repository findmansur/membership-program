package com.firstclub.membership.bootstrap;

import com.firstclub.membership.domain.entity.MembershipPlan;
import com.firstclub.membership.domain.entity.MembershipTier;
import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.entity.User;
import com.firstclub.membership.domain.enums.BenefitType;
import com.firstclub.membership.domain.enums.BillingCycle;
import com.firstclub.membership.domain.enums.CriteriaType;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seeds plans, tiers, benefits, eligibility criteria and a couple of users on
 * application startup so the API is usable end-to-end without any manual
 * setup. Idempotent — checks for existing rows before inserting.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @Transactional
    public ApplicationRunner seedData(MembershipPlanRepository planRepo,
                                      MembershipTierRepository tierRepo,
                                      UserRepository userRepo) {
        return args -> {
            seedPlans(planRepo);
            seedTiers(tierRepo);
            seedUsers(userRepo);
            log.info("Seed data loaded: plans={}, tiers={}, users={}",
                    planRepo.count(), tierRepo.count(), userRepo.count());
        };
    }

    private void seedPlans(MembershipPlanRepository repo) {
        if (repo.count() > 0) return;
        repo.save(new MembershipPlan("MONTHLY",   "Monthly Plan",   BillingCycle.MONTHLY,   new BigDecimal("199.00")));
        repo.save(new MembershipPlan("QUARTERLY", "Quarterly Plan", BillingCycle.QUARTERLY, new BigDecimal("499.00")));
        repo.save(new MembershipPlan("YEARLY",    "Yearly Plan",    BillingCycle.YEARLY,    new BigDecimal("1799.00")));
    }

    private void seedTiers(MembershipTierRepository repo) {
        if (repo.count() > 0) return;

        MembershipTier silver = new MembershipTier(
                "SILVER", "Silver",
                "Entry tier — open to all subscribers.", 1);
        silver.addBenefit(new TierBenefit(
                BenefitType.FREE_DELIVERY,
                "{\"minOrderValue\":499}",
                "Free delivery on orders above 499"));
        silver.addBenefit(new TierBenefit(
                BenefitType.PERCENT_DISCOUNT,
                "{\"percent\":5, \"maxDiscount\":100}",
                "5% off, max 100"));
        repo.save(silver);

        MembershipTier gold = new MembershipTier(
                "GOLD", "Gold",
                "Mid tier — for active shoppers.", 2);
        gold.addBenefit(new TierBenefit(
                BenefitType.FREE_DELIVERY,
                "{\"minOrderValue\":0}",
                "Free delivery on all eligible orders"));
        gold.addBenefit(new TierBenefit(
                BenefitType.PERCENT_DISCOUNT,
                "{\"percent\":10, \"maxDiscount\":500, \"categories\":[\"FASHION\",\"BEAUTY\"]}",
                "10% off on Fashion & Beauty"));
        gold.addBenefit(new TierBenefit(
                BenefitType.EXCLUSIVE_DEALS,
                "{\"couponBundle\":\"GOLD_BUNDLE\"}",
                "Access to Gold-only deals"));
        gold.addBenefit(new TierBenefit(
                BenefitType.EARLY_ACCESS,
                "{\"hoursAhead\":12}",
                "12-hour early access to sales"));
        gold.addEligibilityCriterion(new TierEligibilityCriterion(
                CriteriaType.ORDER_COUNT, new BigDecimal("3"), null, 30, true));
        repo.save(gold);

        MembershipTier platinum = new MembershipTier(
                "PLATINUM", "Platinum",
                "Top tier — exclusive perks for heavy spenders or VIP cohort.", 3);
        platinum.addBenefit(new TierBenefit(
                BenefitType.FREE_DELIVERY,
                "{\"minOrderValue\":0}",
                "Free express delivery on all orders"));
        platinum.addBenefit(new TierBenefit(
                BenefitType.PERCENT_DISCOUNT,
                "{\"percent\":15, \"maxDiscount\":2000}",
                "15% off on everything, up to 2000"));
        platinum.addBenefit(new TierBenefit(
                BenefitType.EXCLUSIVE_DEALS,
                "{\"couponBundle\":\"PLATINUM_BUNDLE\"}",
                "Access to Platinum-only deals"));
        platinum.addBenefit(new TierBenefit(
                BenefitType.EARLY_ACCESS,
                "{\"hoursAhead\":24}",
                "24-hour early access to sales"));
        platinum.addBenefit(new TierBenefit(
                BenefitType.PRIORITY_SUPPORT,
                "{\"channel\":\"PHONE\", \"slaMinutes\":15}",
                "Priority phone support, 15-minute SLA"));
        // Any of: VIP cohort, 10+ orders in 30 days, or 50k+ spend in 30 days.
        platinum.addEligibilityCriterion(new TierEligibilityCriterion(
                CriteriaType.COHORT, null, "VIP", null, false));
        platinum.addEligibilityCriterion(new TierEligibilityCriterion(
                CriteriaType.ORDER_COUNT, new BigDecimal("10"), null, 30, false));
        platinum.addEligibilityCriterion(new TierEligibilityCriterion(
                CriteriaType.ORDER_VALUE, new BigDecimal("50000"), null, 30, false));
        repo.save(platinum);
    }

    private void seedUsers(UserRepository repo) {
        if (repo.count() > 0) return;
        repo.save(new User("Demo User",    "demo@firstclub.com",    "REGULAR"));
        repo.save(new User("VIP User",     "vip@firstclub.com",     "VIP"));
        repo.save(new User("Employee User","emp@firstclub.com",     "EMPLOYEE"));
    }
}
