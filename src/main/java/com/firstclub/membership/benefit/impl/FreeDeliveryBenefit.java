package com.firstclub.membership.benefit.impl;

import com.firstclub.membership.benefit.Benefit;
import com.firstclub.membership.benefit.BenefitConfigParser;
import com.firstclub.membership.benefit.BenefitContext;
import com.firstclub.membership.benefit.BenefitResult;
import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.enums.BenefitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Waives the delivery fee for eligible carts.
 *
 * <p>Config keys:
 * <ul>
 *   <li>{@code minOrderValue} (number, optional, default 0) — minimum cart
 *       subtotal at which the benefit kicks in.</li>
 * </ul>
 */
@Component
public class FreeDeliveryBenefit implements Benefit {

    private final BenefitConfigParser parser;

    public FreeDeliveryBenefit(BenefitConfigParser parser) {
        this.parser = parser;
    }

    @Override
    public BenefitType type() {
        return BenefitType.FREE_DELIVERY;
    }

    @Override
    public BenefitResult apply(BenefitContext context, TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        BigDecimal minOrderValue = BigDecimal.valueOf(
                BenefitConfigParser.numberOrDefault(config, "minOrderValue", 0).doubleValue());

        if (context.getSubtotal().compareTo(minOrderValue) < 0) {
            return BenefitResult.none(type(),
                    "Free delivery applies on orders above " + minOrderValue);
        }
        return BenefitResult.freeDelivery(type(),
                "Free delivery applied (saved " + context.getDeliveryFee() + ")");
    }

    @Override
    public String summarize(TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        Number minOrderValue = BenefitConfigParser.numberOrDefault(config, "minOrderValue", 0);
        if (minOrderValue.doubleValue() <= 0d) {
            return "Free delivery on all eligible orders";
        }
        return "Free delivery on orders above " + minOrderValue;
    }
}
