package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.BenefitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A configured benefit attached to a tier.
 *
 * <p>The behaviour of the benefit comes from a {@link com.firstclub.membership.benefit.Benefit}
 * keyed by {@link #type}; the parameters of that behaviour are stored in
 * {@link #configJson}. Storing parameters as JSON allows adding new benefit
 * shapes (e.g. tiered discounts, capped delivery counts) without schema
 * migrations. Validation of the JSON is the Benefit implementation's job.
 */
@Entity
@Table(name = "tier_benefits")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BenefitType type;

    /**
     * Free-form JSON configuration consumed by the Benefit implementation.
     * Example shapes:
     *   PERCENT_DISCOUNT  : {"percent": 10, "maxDiscount": 500, "categories": ["FASHION"]}
     *   FREE_DELIVERY     : {"minOrderValue": 0}
     *   PRIORITY_SUPPORT  : {"channel": "PHONE", "slaMinutes": 15}
     */
    @Column(name = "config_json", length = 2048)
    private String configJson;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 256)
    private String description;

    public TierBenefit(BenefitType type, String configJson, String description) {
        this.type = type;
        this.configJson = configJson;
        this.description = description;
    }
}
