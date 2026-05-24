import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 15 }, // Ramp up to 15 virtual users
        { duration: '30s', target: 15 }, // Stay at 15 VUs
        { duration: '10s', target: 0 },  // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'], // 95% of queries must finish in under 300ms
        http_req_failed: ['rate<0.01'],    // Error rate must be less than 1%
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Setup authentication
export function setup() {
    const payload = JSON.stringify({
        username: 'analyst',
        password: 'password',
    });
    
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    const res = http.post(`${BASE_URL}/api/v1/auth/token`, payload, params);
    
    check(res, {
        'Auth successful': (r) => r.status === 200,
    });
    
    return { token: res.json('token') };
}

export default function (data) {
    const endpoints = [
        '/api/v1/metrics/revenue',
        '/api/v1/metrics/orders',
        '/api/v1/metrics/refunds',
        '/api/v1/metrics/top-products?limit=5',
        '/api/v1/metrics/customer-activity',
        '/api/v1/metrics/active-customers',
        '/api/v1/metrics/events?page=0&size=10'
    ];
    
    // Pick a random endpoint to stress-test
    const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
    
    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
        },
    };
    
    const res = http.get(`${BASE_URL}${endpoint}`, params);
    
    check(res, {
        'Status OK': (r) => r.status === 200,
    });
    
    sleep(0.2); // pacing interval
}
