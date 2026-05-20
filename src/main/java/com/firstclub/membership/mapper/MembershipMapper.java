package com.firstclub.membership.mapper;

import com.firstclub.membership.domain.dto.OrderDto;
import com.firstclub.membership.domain.dto.PlanDto;
import com.firstclub.membership.domain.dto.SubscriptionDto;
import com.firstclub.membership.domain.dto.TierBenefitDto;
import com.firstclub.membership.domain.dto.TierDto;
import com.firstclub.membership.domain.dto.UserDto;
import com.firstclub.membership.domain.entity.MembershipPlan;
import com.firstclub.membership.domain.entity.MembershipTier;
import com.firstclub.membership.domain.entity.OrderRecord;
import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.entity.TierBenefit;
import com.firstclub.membership.domain.entity.TierEligibilityCriterion;
import com.firstclub.membership.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface MembershipMapper {

    /**
     * Maps MembershipPlan entity to PlanDto.
     */
    @Mapping(target = "durationDays", source = "billingCycle.durationDays")
    @Mapping(target = "price", source = "basePrice")
    PlanDto toPlanDto(MembershipPlan plan);

    /**
     * Maps list of MembershipPlan entities to list of PlanDto.
     */
    List<PlanDto> toPlanDtos(List<MembershipPlan> plans);

    /**
     * Maps MembershipTier entity to TierDto.
     */
    @Mapping(target = "benefits", source = "benefits")
    @Mapping(target = "eligibilityCriteria", source = "eligibilityCriteria", qualifiedByName = "criteriaToStrings")
    TierDto toTierDto(MembershipTier tier);

    /**
     * Maps list of MembershipTier entities to list of TierDto.
     */
    List<TierDto> toTierDtos(List<MembershipTier> tiers);

    /**
     * Maps TierBenefit entity to TierBenefitDto.
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "summary", source = "configJson")
    TierBenefitDto toTierBenefitDto(TierBenefit benefit);

    /**
     * Maps list of TierBenefit entities to list of TierBenefitDto.
     */
    List<TierBenefitDto> toTierBenefitDtos(List<TierBenefit> benefits);

    /**
     * Maps Subscription entity to SubscriptionDto.
     */
    @Mapping(target = "planCode", source = "plan.code")
    @Mapping(target = "tierCode", source = "tier.code")
    SubscriptionDto toSubscriptionDto(Subscription subscription);

    /**
     * Maps list of Subscription entities to list of SubscriptionDto.
     */
    List<SubscriptionDto> toSubscriptionDtos(List<Subscription> subscriptions);

    UserDto toUserDto(User user);

    List<UserDto> toUserDtos(List<User> users);

    OrderDto toOrderDto(OrderRecord order);

    List<OrderDto> toOrderDtos(List<OrderRecord> orders);

    @Named("criteriaToStrings")
    default List<String> criteriaToStrings(List<TierEligibilityCriterion> criteria) {
        return criteria.stream()
                .map(c -> {
                    StringBuilder sb = new StringBuilder(c.getType().name());
                    if (c.getThreshold() != null) {
                        sb.append(" >= ").append(c.getThreshold());
                    }
                    if (c.getStringValue() != null) {
                        sb.append(" = '").append(c.getStringValue()).append("'");
                    }
                    if (c.getWindowDays() != null) {
                        sb.append(" (").append(c.getWindowDays()).append(" days)");
                    }
                    sb.append(c.isRequired() ? " [required]" : " [optional]");
                    return sb.toString();
                })
                .toList();
    }
}
