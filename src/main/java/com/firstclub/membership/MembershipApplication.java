package com.firstclub.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the FirstClub Membership Program backend.
 *
 * <p>The service exposes REST endpoints to manage subscription plans, tiers,
 * configurable benefits and user subscriptions, and integrates with a
 * lightweight order/checkout flow to demonstrate how membership perks are
 * applied at runtime.
 */
@SpringBootApplication
@EnableScheduling
public class MembershipApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembershipApplication.class, args);
    }
}
