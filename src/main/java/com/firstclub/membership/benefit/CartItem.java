package com.firstclub.membership.benefit;

import java.math.BigDecimal;

/**
 * A single line item used by checkout-time benefits. Kept as a simple record
 * (immutable, value-based equality) since benefits should not mutate the cart.
 */
public record CartItem(String sku, String category, BigDecimal price, int quantity) {

    public BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
