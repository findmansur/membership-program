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
 * Informational benefit — surfaces access to exclusive deals/coupons. The
 * actual coupon catalogue would live in a separate service; here we just
 * advertise the entitlement.
 */
@Component
public class ExclusiveDealsBenefit implements Benefit {

    private final BenefitConfigParser parser;

    public ExclusiveDealsBenefit(BenefitConfigParser parser) {
        this.parser = parser;
    }

    @Override
    public BenefitType type() {
        return BenefitType.EXCLUSIVE_DEALS;
    }

    @Override
    public BenefitResult apply(BenefitContext context, TierBenefit configuration) {
        return BenefitResult.informational(type(), summarize(configuration));
    }

    @Override
    public String summarize(TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        String coupon = BenefitConfigParser.stringOrDefault(config, "couponBundle", "EXCLUSIVE");
        return "Access to exclusive deals bundle: " + coupon;
    }
}
