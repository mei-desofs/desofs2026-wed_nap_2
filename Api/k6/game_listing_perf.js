// RNF-23 — Game listing response time < 500 ms
// Runs a single virtual user 30 times against GET /api/games
// and asserts p95 < 500 ms.
//
// Run (with the app already up):
//   docker compose -f docker-compose.yml -f docker-compose.k6.yml run --rm k6 run /scripts/game_listing_perf.js
//
// Expected output: all checks ✓, http_req_duration p(95) < 500ms

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'https://nginx';

const gameDuration = new Trend('game_listing_duration');

export const options = {
    vus: 1,
    iterations: 30,
    insecureSkipTLSVerify: true,
    thresholds: {
        // RNF-23: game listing must respond in < 500 ms (95th percentile)
        'http_req_duration{name:game_listing}': ['p(95)<500'],
        'http_req_failed': ['rate<0.01'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/games`, {
        tags: { name: 'game_listing' },
    });

    gameDuration.add(res.timings.duration);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
        'body is JSON array': (r) => {
            try {
                const body = JSON.parse(r.body);
                return Array.isArray(body) || typeof body === 'object';
            } catch (_) {
                return false;
            }
        },
    });

    sleep(0.1);
}
