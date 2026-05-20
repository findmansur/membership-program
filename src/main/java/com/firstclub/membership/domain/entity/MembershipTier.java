package com.firstclub.membership.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A tier (e.g. Silver, Gold, Platinum) that determines which benefits a
 * subscriber receives. Tiers form an implicit hierarchy via {@link #rank}:
 * upgrade/downgrade operations compare ranks instead of names so new tiers
 * (e.g. "Titanium") can be inserted between existing ones without code changes.
 */
@Entity
@Table(
        name = "membership_tiers",
        uniqueConstraints = @UniqueConstraint(columnNames = "code")
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    /** Higher rank == better tier. Used for upgrade/downgrade comparisons. */
    @Column(nullable = false)
    private int rank;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TierBenefit> benefits = new ArrayList<>();

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TierEligibilityCriterion> eligibilityCriteria = new ArrayList<>();

    public MembershipTier(String code, String name, String description, int rank) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.rank = rank;
    }

    public void addBenefit(TierBenefit benefit) {
        benefit.setTier(this);
        this.benefits.add(benefit);
    }

    public void addEligibilityCriterion(TierEligibilityCriterion criterion) {
        criterion.setTier(this);
        this.eligibilityCriteria.add(criterion);
    }
}
