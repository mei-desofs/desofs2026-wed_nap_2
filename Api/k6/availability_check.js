// RNF-26 — System availability ≥ 99 %
// Simulates continuous health monitoring over 5 minutes (300 s).
// A request failure (non-200 or timeout) counts as unavailability.
//
// Run (with the app already up):
//   docker compose -f docker-compose.yml -f docker-compose.k6.yml run --rm k6 run /scripts/availability_check.js
//
// Threshold: success rate > 99 % across 300 requests (1 VU × ~1 req/s for 300 s).
// This validates that the auto-restart policy and health checks (RNF-26) keep
// the application available without manual intervention.

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'https://nginx';

const availabilityRate = new Rate('availability');

export const options = {
    vus: 1,
    duration: '5m',
    insecureSkipTLSVerify: true,
    thresholds: {
        // RNF-26: must be available ≥ 99 % of the time
        'availability': ['rate>0.99'],
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<2000'],
    },
};

export default function () {
    // ── Spring Boot Actuator health (app layer) ────────────────────────────────
    const appHealth = http.get(`${BASE_URL}/actuator/health`, {
        timeout: '5s',
        tags: { name: 'app_health' },
    });
    const appUp = check(appHealth, {
        'app is UP (200)': (r) => r.status === 200,
        'app status is UP': (r) => r.body.includes('"UP"') || r.body.includes('"status":"UP"'),
    });
    availabilityRate.add(appUp ? 1 : 0);

    // ── Public API smoke check ─────────────────────────────────────────────────
    const apiCheck = http.get(`${BASE_URL}/api/games`, {
        timeout: '5s',
        tags: { name: 'api_smoke' },
    });
    check(apiCheck, {
        'api responds (200)': (r) => r.status === 200,
    });

    sleep(1);
}
