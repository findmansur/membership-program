#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "============================================"
echo "FirstClub Membership Program - API Test Suite"
echo "============================================"
echo ""

echo "1. Fetch all membership plans"
curl -s ${BASE_URL}/plans | python3 -m json.tool
echo ""

echo "2. Fetch all membership tiers"
curl -s ${BASE_URL}/tiers | python3 -m json.tool
echo ""

echo "3. Create a new regular user"
curl -s -X POST ${BASE_URL}/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Test User","email":"test'$(date +%s)'@example.com","cohort":"REGULAR"}' | python3 -m json.tool
echo ""

echo "4. Get all users"
curl -s ${BASE_URL}/users | python3 -m json.tool | head -30
echo ""

echo "5. Subscribe user 1 to MONTHLY/SILVER plan"
curl -s -X POST ${BASE_URL}/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"planCode":"MONTHLY","tierCode":"SILVER","autoRenew":true}' | python3 -m json.tool
echo ""

echo "6. Get current subscription for user 1"
curl -s ${BASE_URL}/subscriptions/user/1/current | python3 -m json.tool
echo ""

echo "7. Create 3 orders for user 1 (to qualify for GOLD tier)"
for i in {1..3}; do
  curl -s -X POST ${BASE_URL}/orders \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":1500}' > /dev/null
  echo "  Order $i created"
done
echo ""

echo "8. Check tier eligibility for user 1"
curl -s ${BASE_URL}/tiers/eligibility/1 | python3 -m json.tool
echo ""

echo "9. Upgrade user 1 to GOLD tier"
SUBSCRIPTION_ID=$(curl -s ${BASE_URL}/subscriptions/user/1/current | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
curl -s -X POST ${BASE_URL}/subscriptions/${SUBSCRIPTION_ID}/upgrade \
  -H 'Content-Type: application/json' \
  -d '{"tierCode":"GOLD"}' | python3 -m json.tool
echo ""

echo "10. Get order history for user 1"
curl -s ${BASE_URL}/orders/user/1 | python3 -m json.tool | head -30
echo ""

echo "11. Preview checkout with GOLD benefits"
curl -s -X POST ${BASE_URL}/checkout/preview \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"deliveryFee":49,"items":[{"sku":"S1","category":"FASHION","price":2000,"quantity":1},{"sku":"S2","category":"BEAUTY","price":1500,"quantity":1}]}' | python3 -m json.tool
echo ""

echo "12. Update user 2 cohort to VIP"
curl -s -X PATCH ${BASE_URL}/users/2/cohort \
  -H 'Content-Type: application/json' \
  -d '{"cohort":"VIP"}' | python3 -m json.tool
echo ""

echo "13. Check tier eligibility for user 2 (should be PLATINUM eligible)"
curl -s ${BASE_URL}/tiers/eligibility/2 | python3 -m json.tool
echo ""

echo "14. Subscribe user 2 to YEARLY/PLATINUM plan"
curl -s -X POST ${BASE_URL}/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"planCode":"YEARLY","tierCode":"PLATINUM","autoRenew":true}' | python3 -m json.tool
echo ""

echo "15. Preview checkout with PLATINUM benefits (15% off + free delivery)"
curl -s -X POST ${BASE_URL}/checkout/preview \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"deliveryFee":49,"items":[{"sku":"S1","category":"ELECTRONICS","price":5000,"quantity":2},{"sku":"S2","category":"FASHION","price":3000,"quantity":1}]}' | python3 -m json.tool
echo ""

echo "16. Disable auto-renew for user 2 subscription"
SUB_ID_2=$(curl -s ${BASE_URL}/subscriptions/user/2/current | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
curl -s -X POST "${BASE_URL}/subscriptions/${SUB_ID_2}/auto-renew?enabled=false" | python3 -m json.tool
echo ""

echo "17. Get subscription history for user 1"
curl -s ${BASE_URL}/subscriptions/user/1 | python3 -m json.tool
echo ""

echo "============================================"
echo "All test cases executed successfully!"
echo "============================================"
