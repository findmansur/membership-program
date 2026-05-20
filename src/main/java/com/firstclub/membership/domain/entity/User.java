package com.firstclub.membership.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Minimal user model. The real platform owns its own identity service; here we
 * keep only the fields the membership module actually needs (cohort for tier
 * eligibility, email for support routing).
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 128)
    private String email;

    /** Marketing/internal cohort label, e.g. "VIP", "EMPLOYEE", "STUDENT". */
    @Column(length = 64)
    private String cohort;

    public User(String name, String email, String cohort) {
        this.name = name;
        this.email = email;
        this.cohort = cohort;
    }
}
