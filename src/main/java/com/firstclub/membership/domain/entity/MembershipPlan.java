package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.BillingCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A billing plan a user can subscribe to (e.g. Monthly@499, Yearly@4999).
 *
 * <p>Plans are decoupled from tiers: any plan can be paired with any
 * available tier at subscription time. This keeps the two axes — payment
 * frequency vs. benefit level — independent.
 */
@Entity
@Table(
        name = "membership_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = "code")
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable machine code used by clients (e.g. "MONTHLY", "YEARLY"). */
    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BillingCycle billingCycle;

    /**
     * Base price for the plan irrespective of tier. Tier-specific
     * surcharges (premium pricing) can be added later without changing the
     * schema by introducing a pricing strategy on top of this field.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private boolean active = true;

    public MembershipPlan(String code, String name, BillingCycle billingCycle, BigDecimal basePrice) {
        this.code = code;
        this.name = name;
        this.billingCycle = billingCycle;
        this.basePrice = basePrice;
    }
}
