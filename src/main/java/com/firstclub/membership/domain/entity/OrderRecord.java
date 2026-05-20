package com.firstclub.membership.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Lightweight order record used to evaluate order-based tier criteria. It is
 * intentionally minimal — the real Orders service would publish events the
 * membership module would subscribe to, but for the demo we expose a simple
 * REST endpoint that records orders directly.
 *
 * <p>Named {@code OrderRecord} (not {@code Order}) because {@code ORDER} is a
 * reserved word in SQL.
 */
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_created", columnList = "user_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OrderRecord(Long userId, BigDecimal amount, Instant createdAt) {
        this.userId = userId;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
