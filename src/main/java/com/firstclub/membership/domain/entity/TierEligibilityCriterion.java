package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.CriteriaType;
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

import java.math.BigDecimal;

/**
 * A rule used to decide whether a user qualifies for a tier.
 *
 * <p>A tier can have multiple criteria. Whether all of them must be satisfied
 * (AND) or any of them (OR) is controlled per criterion via {@link #required}:
 * required criteria are AND-ed together while non-required criteria are OR-ed.
 * This gives flexibility without needing a separate boolean-expression language.
 */
@Entity
@Table(name = "tier_eligibility_criteria")
public class TierEligibilityCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CriteriaType type;

    /**
     * Numeric threshold for the criterion. Interpretation depends on
     * {@link #type}: order count, total order value, etc.
     */
    @Column(precision = 14, scale = 2)
    private BigDecimal threshold;

    /**
     * Optional string value for criteria like cohort matching.
     */
    @Column(length = 128)
    private String stringValue;

    /**
     * Look-back window in days. Used for time-bounded criteria such as
     * "orders placed in the last 30 days".
     */
    @Column(name = "window_days")
    private Integer windowDays;

    @Column(nullable = false)
    private boolean required = true;

    protected TierEligibilityCriterion() {
    }

    public TierEligibilityCriterion(CriteriaType type, BigDecimal threshold,
                                    String stringValue, Integer windowDays, boolean required) {
        this.type = type;
        this.threshold = threshold;
        this.stringValue = stringValue;
        this.windowDays = windowDays;
        this.required = required;
    }

    public Long getId() {
        return id;
    }

    public MembershipTier getTier() {
        return tier;
    }

    public void setTier(MembershipTier tier) {
        this.tier = tier;
    }

    public CriteriaType getType() {
        return type;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Integer getWindowDays() {
        return windowDays;
    }

    public boolean isRequired() {
        return required;
    }
}
