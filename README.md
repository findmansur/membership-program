# FirstClub Membership Program

Backend service for a tier-based subscription membership program. Users pick a
billing plan (Monthly / Quarterly / Yearly) and a tier (Silver / Gold / Platinum),
get a configurable bundle of benefits (free delivery, percent discount, exclusive
deals, priority support, early access) and can be auto-upgraded as their order
behaviour qualifies them for higher tiers.

Built with **Java 21 + Spring Boot 3.3**, embedded **H2** so it runs out of the
box with no external dependencies.

---

## 1. Design at a glance

```
                +-----------------+        +----------------+
                | MembershipPlan  |        | MembershipTier |
                | (Monthly / Q/Y) |        | (rank-ordered) |
                +--------+--------+        +--------+-------+
                         |                          |
                         |                          |---< TierBenefit (BenefitType + JSON config)
                         |                          |---< TierEligibilityCriterion (CriteriaType)
                         |                          |
                         +----+    +----------------+
                              |    |
                              v    v
                       +----------------+      +-------------------+
                       |  Subscription  |----->| SubscriptionEvent |
                       | (User + Plan + |      |  (audit log)      |
                       |  Tier + dates) |      +-------------------+
                       +----------------+
```

Key abstractions:

| Concept | Type | Why |
|--|--|--|
| `Benefit` strategy | interface keyed by `BenefitType` | New benefit types plug in by adding an enum value and a Spring bean — no other code changes |
| `TierEligibilityRule` | interface keyed by `CriteriaType` | Same idea for tier-promotion rules |
| `TierEligibilityEvaluator` | composite | Combines per-tier criteria (required AND, optional OR) |
| `KeyedLock<Long>` | per-user mutex | Serialises concurrent subscribe/upgrade/cancel calls for one user without blocking other users |
| `@Version` on `Subscription` | optimistic lock | Defence-in-depth against stale background writers |

### Extensibility

* **Adding a new benefit (e.g. cashback)** — add `BenefitType.CASHBACK`, write a
  `CashbackBenefit implements Benefit` bean. Wire-up is automatic via the
  `BenefitRegistry`. Configure per-tier parameters as JSON.
* **Adding a new tier (e.g. Titanium)** — insert a row with a `rank` value
  between two existing tiers. Upgrade/downgrade compare ranks, so no code change.
* **Adding a new eligibility rule (e.g. cart-abandonment rate)** — add a
  `CriteriaType` value and a `TierEligibilityRule` bean. Reference it from
  `TierEligibilityCriterion` rows.

### Concurrency model

1. Every mutating operation in `SubscriptionService` runs inside
   `KeyedLock.executeLocked(userId, ...)`. Concurrent calls for the same user
   serialize; calls for different users run in parallel.
2. The lock is taken **around** the transaction (using `TransactionTemplate`)
   so the DB commits and releases row locks before the next waiter reads the
   latest state — no read-your-own-writes races.
3. `@Version` on `Subscription` is the safety net for any writer that bypasses
   the service (e.g. a future Kafka consumer). Conflicts surface as HTTP 409
   via `GlobalExceptionHandler`.
4. `KeyedLock` is reference-counted, so unused lock entries are garbage
   collected — the map cannot grow unboundedly over time.
5. Background jobs (`SubscriptionExpiryJob`, `TierReevaluationJob`) reuse the
   same service entry points, so they participate in the same locking
   discipline.

---

## 2. Running the service

```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`. The H2 console is at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:membership`, user `sa`,
no password).

The `DataSeeder` populates three plans, three tiers (with benefits + eligibility
rules) and three demo users on first start.

To run unit tests / build a fat jar:

```bash
mvn package            # produces target/membership-program-1.0.0.jar
java -jar target/membership-program-1.0.0.jar
```

---

## 3. REST API

All endpoints are versioned under `/api/v1`.

### Plans

```http
GET /api/v1/plans
```

### Tiers

```http
GET /api/v1/tiers
GET /api/v1/tiers/eligibility/{userId}
```

### Users (demo helpers)

```http
POST /api/v1/users         { "name":"...", "email":"...", "cohort":"VIP" }
GET  /api/v1/users
GET  /api/v1/users/{id}
```

### Orders (drives tier eligibility)

```http
POST /api/v1/orders        { "userId":1, "amount":1200.00 }
```

### Subscriptions

```http
POST /api/v1/subscriptions             { "userId":1, "planCode":"MONTHLY", "tierCode":"SILVER", "autoRenew":true }
GET  /api/v1/subscriptions/user/{id}/current
GET  /api/v1/subscriptions/user/{id}
POST /api/v1/subscriptions/{id}/upgrade    { "tierCode":"GOLD" }
POST /api/v1/subscriptions/{id}/downgrade  { "tierCode":"SILVER" }
POST /api/v1/subscriptions/{id}/cancel?immediate=false
POST /api/v1/subscriptions/{id}/renew
```

### Checkout preview (demo integration)

```http
POST /api/v1/checkout/preview
{
  "userId": 2,
  "deliveryFee": 49.00,
  "items": [
    { "sku":"SKU-1", "category":"FASHION", "price":1000.00, "quantity":2 },
    { "sku":"SKU-2", "category":"GROCERY", "price":300.00,  "quantity":1 }
  ]
}
```

The response lists every benefit applied (with description, discount, and
whether delivery was waived), the total discount and the final amount.

---

## 4. End-to-end demo (curl)

```bash
# 1. See plans and tiers
curl -s http://localhost:8080/api/v1/plans   | jq
curl -s http://localhost:8080/api/v1/tiers   | jq

# 2. Subscribe regular user (id=1) to Monthly / Silver
curl -s -X POST http://localhost:8080/api/v1/subscriptions \
     -H 'Content-Type: application/json' \
     -d '{"userId":1,"planCode":"MONTHLY","tierCode":"SILVER","autoRenew":true}' | jq

# 3. Drive order volume so user becomes Gold-eligible (3 orders in 30 days)
for i in 1 2 3; do
  curl -s -X POST http://localhost:8080/api/v1/orders \
       -H 'Content-Type: application/json' \
       -d '{"userId":1,"amount":1500}' >/dev/null
done

# 4. Check tier eligibility
curl -s http://localhost:8080/api/v1/tiers/eligibility/1 | jq

# 5. Upgrade
curl -s -X POST http://localhost:8080/api/v1/subscriptions/1/upgrade \
     -H 'Content-Type: application/json' \
     -d '{"tierCode":"GOLD"}' | jq

# 6. Preview a cart and see benefits applied
curl -s -X POST http://localhost:8080/api/v1/checkout/preview \
     -H 'Content-Type: application/json' \
     -d '{"userId":1,"deliveryFee":49,"items":[
          {"sku":"S1","category":"FASHION","price":1000,"quantity":2},
          {"sku":"S2","category":"GROCERY","price":300,"quantity":1}]}' | jq

# 7. VIP user (id=2) qualifies for Platinum on cohort alone
curl -s http://localhost:8080/api/v1/tiers/eligibility/2 | jq
```

---

## 5. Project layout

```
src/main/java/com/firstclub/membership/
├── MembershipApplication.java
├── benefit/                 # Benefit strategy interface + implementations
│   ├── Benefit.java
│   ├── BenefitRegistry.java
│   ├── BenefitConfigParser.java
│   └── impl/ (FreeDelivery, PercentDiscount, ExclusiveDeals, PrioritySupport, EarlyAccess)
├── bootstrap/               # Data seeding
├── concurrency/             # KeyedLock<K>
├── controller/              # REST endpoints
├── domain/
│   ├── entity/              # JPA entities
│   ├── enums/               # BillingCycle, BenefitType, CriteriaType, …
│   └── dto/                 # Request/response records
├── exception/               # ApiException hierarchy + GlobalExceptionHandler
├── repository/              # Spring Data JPA repositories
├── scheduler/               # SubscriptionExpiryJob, TierReevaluationJob
├── service/                 # PlanService, TierService, SubscriptionService, CheckoutService, …
└── tier/                    # TierEligibilityRule + evaluator
```
