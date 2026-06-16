// RNF-24 — 100+ concurrent users with zero errors and p95 latency < 1 s
//
// Run (with the app already up):
//   docker compose -f docker-compose.yml -f docker-compose.k6.yml run --rm k6 run /scripts/load_test.js
//
// Scenario:
//   - Ramp up to 100 VUs over 15 s
//   - Hold 100 VUs for 60 s
//   - Ramp down over 10 s
//
// Thresholds (RNF-24 pass criteria):
//   - Error rate < 1 % (zero errors expected)
//   - p95 response time < 1 000 ms
//   - p99 response time < 2 000 ms

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'https://nginx';

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '15s', target: 100 }, // ramp up to 100 VUs
        { duration: '60s', target: 100 }, // hold 100 VUs
        { duration: '10s', target: 0   }, // ramp down
    ],
    thresholds: {
        // RNF-24: p95 < 1 s, p99 < 2 s
        'http_req_duration': ['p(95)<1000', 'p(99)<2000'],
        // zero errors (< 1 % failed requests)
        'http_req_failed': ['rate<0.01'],
        // all checks must pass
        'checks': ['rate>0.99'],
    },
};

export default function () {
    // ── Public game listing ────────────────────────────────────────────────────
    const gamesRes = http.get(`${BASE_URL}/api/games`, {
        tags: { name: 'game_listing' },
    });
    check(gamesRes, {
        'GET /api/games → 200': (r) => r.status === 200,
        'game listing < 1 s':   (r) => r.timings.duration < 1000,
    });

    sleep(0.5);

    // ── Health endpoint ────────────────────────────────────────────────────────
    const healthRes = http.get(`${BASE_URL}/actuator/health`, {
        tags: { name: 'health' },
    });
    check(healthRes, {
        'GET /actuator/health → 200': (r) => r.status === 200,
        'status is UP':               (r) => r.body.includes('"UP"') || r.body.includes('"status":"UP"'),
    });

    sleep(0.3);
}
