package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.SubscriptionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionEventRepository extends JpaRepository<SubscriptionEvent, Long> {

    List<SubscriptionEvent> findAllBySubscriptionIdOrderByOccurredAtAsc(Long subscriptionId);
}
