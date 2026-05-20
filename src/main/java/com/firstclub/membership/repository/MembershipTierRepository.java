package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    Optional<MembershipTier> findByCode(String code);

    List<MembershipTier> findAllByActiveTrueOrderByRankAsc();
}
