package com.firstclub.membership.service;

import com.firstclub.membership.benefit.Benefit;
import com.firstclub.membership.benefit.BenefitContext;
import com.firstclub.membership.benefit.BenefitRegistry;
import com.firstclub.membership.benefit.BenefitResult;
import com.firstclub.membership.benefit.CartItem;
import com.firstclub.membership.domain.dto.CheckoutPreviewRequest;
import com.firstclub.membership.domain.dto.CheckoutPreviewResponse;
import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.entity.TierBenefit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates how the membership module integrates with the shopping
 * journey: given a cart, find the user's active benefits and apply them.
 *
 * <p>The implementation is intentionally pure — it never mutates the cart or
 * persists anything. The output {@link CheckoutPreviewResponse} carries all
 * the information the calling checkout flow needs to display savings to the
 * user and adjust the final amount.
 */
@Service
public class CheckoutService {

    private final SubscriptionService subscriptionService;
    private final BenefitRegistry benefitRegistry;

    public CheckoutService(SubscriptionService subscriptionService, BenefitRegistry benefitRegistry) {
        this.subscriptionService = subscriptionService;
        this.benefitRegistry = benefitRegistry;
    }

    @Transactional(readOnly = true)
    public CheckoutPreviewResponse preview(CheckoutPreviewRequest request) {
        List<CartItem> items = request.items().stream()
                .map(i -> new CartItem(i.sku(), i.category(), i.price(), i.quantity()))
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deliveryFee = request.deliveryFee();
        Optional<Subscription> active = subscriptionService.findCurrent(request.userId());
        BenefitContext context = new BenefitContext(request.userId(), items, subtotal, deliveryFee);

        List<BenefitResult> applied = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal effectiveDelivery = deliveryFee;
        String tierCode = null;

        if (active.isPresent()) {
            Subscription sub = active.get();
            tierCode = sub.getTier().getCode();
            for (TierBenefit configured : sub.getTier().getBenefits()) {
                if (!configured.isActive()) continue;
                Benefit b = benefitRegistry.forType(configured.getType());
                BenefitResult result = b.apply(context, configured);
                applied.add(result);
                totalDiscount = totalDiscount.add(result.discount());
                if (result.deliveryWaived()) {
                    effectiveDelivery = BigDecimal.ZERO;
                }
            }
        }

        BigDecimal finalAmount = subtotal
                .subtract(totalDiscount)
                .add(effectiveDelivery)
                .max(BigDecimal.ZERO);

        return new CheckoutPreviewResponse(
                request.userId(),
                tierCode,
                subtotal,
                deliveryFee,
                totalDiscount,
                finalAmount,
                applied
        );
    }
}
