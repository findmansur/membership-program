package com.firstclub.membership.benefit;

import java.math.BigDecimal;
import java.util.List;

/**
 * Input passed to a {@link Benefit} at application time. Holds the cart and
 * any derived metrics. Building this object once and sharing it across all
 * benefits lets each benefit read what it needs without recomputing totals.
 */
public final class BenefitContext {

    private final Long userId;
    private final List<CartItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal deliveryFee;

    public BenefitContext(Long userId, List<CartItem> items, BigDecimal subtotal, BigDecimal deliveryFee) {
        this.userId = userId;
        this.items = List.copyOf(items);
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
    }

    public Long getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }
}
