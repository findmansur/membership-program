package com.firstclub.membership.benefit.impl;

import com.firstclub.membership.benefit.Benefit;
import com.firstclub.membership.benefit.BenefitConfigParser;
import com.firstclub.membership.benefit.BenefitContext;
import com.firstclub.membership.benefit.BenefitResult;
import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.enums.BenefitType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Informational benefit granting the user early access to sales for a
 * configurable number of hours. Higher tiers get a longer early-access window.
 */
@Component
public class EarlyAccessBenefit implements Benefit {

    private final BenefitConfigParser parser;

    public EarlyAccessBenefit(BenefitConfigParser parser) {
        this.parser = parser;
    }

    @Override
    public BenefitType type() {
        return BenefitType.EARLY_ACCESS;
    }

    @Override
    public BenefitResult apply(BenefitContext context, TierBenefit configuration) {
        return BenefitResult.informational(type(), summarize(configuration));
    }

    @Override
    public String summarize(TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        Number hours = BenefitConfigParser.numberOrDefault(config, "hoursAhead", 12);
        return "Early access to sales " + hours + " hours before public launch";
    }
}
