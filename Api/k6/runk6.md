# Start the main stack first

```bash
docker compose up -d
```

# RNF-23 — Response time < 500ms (game listing)
```bash
docker compose -f docker-compose.yml -f docker-compose.k6.yml run --rm k6 run /scripts/game_listing_perf.js
```

# RNF-24 — 100 concurrent users, 60s
```bash
docker compose -f docker-compose.yml -f docker-compose.k6.yml run --rm k6 run /scripts/load_test.js
```

# RNF-26 — 5-minute availability check (99% uptime)
```bash
docker compose -f docker-compose.yml -f docker-compose.k6.yml run --rm k6 run /scripts/availability_check.js
```
