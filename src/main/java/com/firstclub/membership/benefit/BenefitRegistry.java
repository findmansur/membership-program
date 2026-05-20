package com.firstclub.membership.benefit;

import com.firstclub.membership.domain.enums.BenefitType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Wires every {@link Benefit} bean discovered in the application context into
 * an {@link EnumMap} keyed by {@link BenefitType}. This is the only place that
 * needs to know about all benefit implementations — services depend on this
 * registry, not on individual implementations, so new benefit types plug in
 * without touching call sites.
 */
@Component
public class BenefitRegistry {

    private final Map<BenefitType, Benefit> byType;

    public BenefitRegistry(List<Benefit> benefits) {
        EnumMap<BenefitType, Benefit> map = new EnumMap<>(BenefitType.class);
        for (Benefit b : benefits) {
            Benefit prev = map.put(b.type(), b);
            if (prev != null) {
                throw new IllegalStateException(
                        "Duplicate Benefit beans registered for type " + b.type()
                                + ": " + prev.getClass().getName() + " / " + b.getClass().getName());
            }
        }
        this.byType = Map.copyOf(map);
    }

    public Benefit forType(BenefitType type) {
        Benefit b = byType.get(type);
        if (b == null) {
            throw new IllegalStateException("No Benefit implementation registered for " + type);
        }
        return b;
    }
}
