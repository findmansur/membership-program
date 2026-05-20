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
 * Informational benefit advertising priority support routing for the member.
 * The downstream support system reads the member's tier when a ticket is
 * created — this benefit just records the entitlement and its parameters.
 */
@Component
public class PrioritySupportBenefit implements Benefit {

    private final BenefitConfigParser parser;

    public PrioritySupportBenefit(BenefitConfigParser parser) {
        this.parser = parser;
    }

    @Override
    public BenefitType type() {
        return BenefitType.PRIORITY_SUPPORT;
    }

    @Override
    public BenefitResult apply(BenefitContext context, TierBenefit configuration) {
        return BenefitResult.informational(type(), summarize(configuration));
    }

    @Override
    public String summarize(TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        String channel = BenefitConfigParser.stringOrDefault(config, "channel", "EMAIL");
        Number sla = BenefitConfigParser.numberOrDefault(config, "slaMinutes", 60);
        return "Priority support via " + channel + " (SLA " + sla + " minutes)";
    }
}
