package com.firstclub.membership.tier;

import com.firstclub.membership.domain.entity.MembershipTier;
import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.enums.CriteriaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Decides if a user is eligible for a tier by evaluating that tier's
 * {@link TierEligibilityCriterion}s.
 *
 * <p>Composition rules (kept deliberately simple):
 * <ul>
 *   <li>All {@code required = true} criteria must be satisfied (AND).</li>
 *   <li>If there is at least one {@code required = false} criterion, at least
 *       one of them must be satisfied (OR).</li>
 *   <li>A tier with no criteria is open to everyone (base tier).</li>
 * </ul>
 *
 * <p>This gives flexibility — e.g. "must have ≥10 orders AND (VIP cohort OR
 * spent ≥50k)" — without needing a separate expression language.
 */
@Component
public class TierEligibilityEvaluator {

    private final Map<CriteriaType, TierEligibilityRule> rules;

    public TierEligibilityEvaluator(List<TierEligibilityRule> rules) {
        EnumMap<CriteriaType, TierEligibilityRule> map = new EnumMap<>(CriteriaType.class);
        for (TierEligibilityRule r : rules) {
            TierEligibilityRule prev = map.put(r.type(), r);
            if (prev != null) {
                throw new IllegalStateException(
                        "Duplicate TierEligibilityRule for " + r.type() + ": "
                                + prev.getClass() + " / " + r.getClass());
            }
        }
        this.rules = Map.copyOf(map);
    }

    public EligibilityDecision evaluate(MembershipTier tier, TierEligibilityContext context) {
        List<TierEligibilityCriterion> criteria = tier.getEligibilityCriteria();
        if (criteria.isEmpty()) {
            return new EligibilityDecision(true, List.of("Open to all members"));
        }

        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        boolean allRequiredSatisfied = true;
        boolean anyOptional = false;
        boolean anyOptionalSatisfied = false;

        for (TierEligibilityCriterion c : criteria) {
            TierEligibilityRule rule = rules.get(c.getType());
            if (rule == null) {
                unmatched.add("Unknown rule for " + c.getType());
                allRequiredSatisfied = false;
                continue;
            }
            boolean satisfied = rule.isSatisfied(context, c);
            String desc = rule.describe(c) + (c.isRequired() ? " [required]" : " [optional]");
            if (satisfied) {
                matched.add(desc);
            } else {
                unmatched.add(desc);
            }
            if (c.isRequired()) {
                allRequiredSatisfied = allRequiredSatisfied && satisfied;
            } else {
                anyOptional = true;
                anyOptionalSatisfied = anyOptionalSatisfied || satisfied;
            }
        }

        boolean eligible = allRequiredSatisfied && (!anyOptional || anyOptionalSatisfied);

        List<String> notes = new ArrayList<>();
        notes.addAll(matched.stream().map(s -> "OK: " + s).toList());
        notes.addAll(unmatched.stream().map(s -> "MISSING: " + s).toList());
        return new EligibilityDecision(eligible, notes);
    }

    public record EligibilityDecision(boolean eligible, List<String> notes) {
    }
}
