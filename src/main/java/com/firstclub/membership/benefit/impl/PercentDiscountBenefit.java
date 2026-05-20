package com.firstclub.membership.benefit.impl;

import com.firstclub.membership.benefit.Benefit;
import com.firstclub.membership.benefit.BenefitConfigParser;
import com.firstclub.membership.benefit.BenefitContext;
import com.firstclub.membership.benefit.BenefitResult;
import com.firstclub.membership.benefit.CartItem;
import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.enums.BenefitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applies a percentage discount on cart items, optionally restricted to a
 * configured set of categories, with an optional max discount cap.
 *
 * <p>Config keys:
 * <ul>
 *   <li>{@code percent} (number, required) — percent value 0-100.</li>
 *   <li>{@code categories} (list of strings, optional) — restricts to listed
 *       categories. Empty/missing means "all categories".</li>
 *   <li>{@code maxDiscount} (number, optional) — caps the discount amount.</li>
 * </ul>
 */
@Component
public class PercentDiscountBenefit implements Benefit {

    private final BenefitConfigParser parser;

    public PercentDiscountBenefit(BenefitConfigParser parser) {
        this.parser = parser;
    }

    @Override
    public BenefitType type() {
        return BenefitType.PERCENT_DISCOUNT;
    }

    @Override
    public BenefitResult apply(BenefitContext context, TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        BigDecimal percent = BigDecimal.valueOf(
                BenefitConfigParser.requireNumber(config, "percent").doubleValue());

        Set<String> categories = extractCategories(config);

        BigDecimal eligibleTotal = context.getItems().stream()
                .filter(item -> categories.isEmpty() || categories.contains(item.category()))
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (eligibleTotal.signum() == 0) {
            return BenefitResult.none(type(), percent + "% discount (no eligible items in cart)");
        }

        BigDecimal discount = eligibleTotal
                .multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Number cap = BenefitConfigParser.numberOrDefault(config, "maxDiscount", null);
        if (cap != null) {
            BigDecimal maxDiscount = BigDecimal.valueOf(cap.doubleValue());
            if (discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        }

        String scope = categories.isEmpty() ? "all items" : "categories " + categories;
        return BenefitResult.discount(type(), discount,
                percent + "% off on " + scope + " (saved " + discount + ")");
    }

    @Override
    public String summarize(TierBenefit configuration) {
        Map<String, Object> config = parser.parse(configuration.getConfigJson());
        Number percent = BenefitConfigParser.numberOrDefault(config, "percent", 0);
        Set<String> categories = extractCategories(config);
        Number cap = BenefitConfigParser.numberOrDefault(config, "maxDiscount", null);

        StringBuilder sb = new StringBuilder(percent + "% off");
        if (!categories.isEmpty()) {
            sb.append(" on ").append(categories);
        }
        if (cap != null) {
            sb.append(" (up to ").append(cap).append(")");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractCategories(Map<String, Object> config) {
        Object raw = config.get("categories");
        if (!(raw instanceof Collection<?> c)) {
            return Set.of();
        }
        return ((Collection<Object>) c).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toUnmodifiableSet());
    }

}
