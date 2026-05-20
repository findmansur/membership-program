package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderRecord, Long> {

    @Query("select count(o) from OrderRecord o where o.userId = :userId and o.createdAt >= :since")
    long countOrdersSince(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("select coalesce(sum(o.amount), 0) from OrderRecord o " +
           "where o.userId = :userId and o.createdAt >= :since")
    BigDecimal sumOrderValueSince(@Param("userId") Long userId, @Param("since") Instant since);

    List<OrderRecord> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
