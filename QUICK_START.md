# Quick Start Guide

## Start the Application

```bash
cd /Users/navneetthakur/Desktop/membership-program
mvn spring-boot:run
```

Server starts on `http://localhost:8080`

## Test All Use Cases

Run the comprehensive test suite:

```bash
./test-api.sh
```

Or test manually:

### 1. View Available Plans & Tiers

```bash
# Get all plans
curl http://localhost:8080/api/v1/plans | jq

# Get all tiers with benefits
curl http://localhost:8080/api/v1/tiers | jq
```

### 2. Subscribe to a Plan

```bash
# Subscribe user 1 to MONTHLY/SILVER
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"planCode":"MONTHLY","tierCode":"SILVER","autoRenew":true}' | jq

# Check current subscription
curl http://localhost:8080/api/v1/subscriptions/user/1/current | jq
```

### 3. Drive Tier Eligibility with Orders

```bash
# Create orders to qualify for GOLD (need 3 orders)
for i in {1..3}; do
  curl -X POST http://localhost:8080/api/v1/orders \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":1500}'
done

# Check eligibility
curl http://localhost:8080/api/v1/tiers/eligibility/1 | jq
```

### 4. Upgrade Tier

```bash
# Get subscription ID
SUB_ID=$(curl -s http://localhost:8080/api/v1/subscriptions/user/1/current | jq -r '.id')

# Upgrade to GOLD
curl -X POST http://localhost:8080/api/v1/subscriptions/${SUB_ID}/upgrade \
  -H 'Content-Type: application/json' \
  -d '{"tierCode":"GOLD"}' | jq
```

### 5. Preview Checkout with Benefits

```bash
# Preview cart with GOLD benefits
curl -X POST http://localhost:8080/api/v1/checkout/preview \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":1,
    "deliveryFee":49,
    "items":[
      {"sku":"S1","category":"FASHION","price":2000,"quantity":1},
      {"sku":"S2","category":"BEAUTY","price":1500,"quantity":1}
    ]
  }' | jq
```

### 6. Update User Cohort (for PLATINUM eligibility)

```bash
# Update user 2 to VIP cohort
curl -X PATCH http://localhost:8080/api/v1/users/2/cohort \
  -H 'Content-Type: application/json' \
  -d '{"cohort":"VIP"}' | jq

# Check PLATINUM eligibility
curl http://localhost:8080/api/v1/tiers/eligibility/2 | jq

# Subscribe to PLATINUM
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"planCode":"YEARLY","tierCode":"PLATINUM","autoRenew":true}' | jq
```

### 7. Manage Subscription

```bash
# Disable auto-renew
curl -X POST "http://localhost:8080/api/v1/subscriptions/${SUB_ID}/auto-renew?enabled=false" | jq

# Cancel subscription (benefits remain until end date)
curl -X POST "http://localhost:8080/api/v1/subscriptions/${SUB_ID}/cancel?immediate=false" | jq

# Renew subscription
curl -X POST http://localhost:8080/api/v1/subscriptions/${SUB_ID}/renew | jq

# Get subscription history
curl http://localhost:8080/api/v1/subscriptions/user/1 | jq
```

### 8. View Order History

```bash
curl http://localhost:8080/api/v1/orders/user/1 | jq
```

## H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:membership`
- Username: `sa`
- Password: (leave empty)

### Useful SQL Queries

```sql
-- View all subscriptions with tier and plan details
SELECT s.id, s.user_id, p.code as plan, t.code as tier, s.status, s.auto_renew 
FROM subscriptions s 
JOIN membership_plans p ON s.plan_id = p.id 
JOIN membership_tiers t ON s.tier_id = t.id;

-- View all benefits by tier
SELECT t.code as tier, tb.type, tb.description, tb.config_json 
FROM tier_benefits tb 
JOIN membership_tiers t ON tb.tier_id = t.id 
WHERE tb.active = true 
ORDER BY t.rank, tb.type;

-- View tier eligibility criteria
SELECT t.code as tier, ec.type, ec.threshold, ec.string_value, ec.window_days, ec.required 
FROM tier_eligibility_criteria ec 
JOIN membership_tiers t ON ec.tier_id = t.id 
ORDER BY t.rank;

-- View subscription events (audit log)
SELECT se.subscription_id, se.event_type, se.from_tier, se.to_tier, se.occurred_at 
FROM subscription_events se 
ORDER BY se.occurred_at DESC;

-- User order statistics
SELECT user_id, COUNT(*) as order_count, SUM(amount) as total_spent 
FROM orders 
GROUP BY user_id;
```

## Key Features Demonstrated

✅ **Membership Plans**: Monthly/Quarterly/Yearly with different prices  
✅ **Membership Tiers**: Silver/Gold/Platinum with rank-based hierarchy  
✅ **Configurable Benefits**: Free delivery, % discount, exclusive deals, early access, priority support  
✅ **Tier Eligibility**: Based on order count, order value, or user cohort  
✅ **Subscribe/Upgrade/Downgrade/Cancel**: Full subscription lifecycle  
✅ **Auto-Renewal**: Toggle on/off  
✅ **Checkout Integration**: Real-time benefit application  
✅ **Concurrency**: Per-user locking + optimistic locking  
✅ **Extensibility**: Strategy pattern for benefits and eligibility rules  
✅ **Background Jobs**: Auto-upgrade and expiry handling  

## Architecture Highlights

- **Clean Code**: SOLID principles, design patterns
- **Lombok**: Reduced boilerplate code
- **MapStruct**: Type-safe DTO mapping
- **Spring Validation**: Request validation
- **Concurrency**: KeyedLock + @Version
- **Extensibility**: Add new benefits/rules without code changes
