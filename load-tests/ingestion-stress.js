import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    stages: [
        { duration: '10s', target: 20 }, // Ramp up to 20 virtual users
        { duration: '30s', target: 20 }, // Stay at 20 VUs
        { duration: '10s', target: 0 },  // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests must complete under 500ms
        http_req_failed: ['rate<0.01'],    // Error rate must be less than 1%
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Setup authentication
export function setup() {
    const payload = JSON.stringify({
        username: 'admin',
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
    const eventId = uuidv4();
    const orderId = uuidv4();
    
    const payload = JSON.stringify({
        orderId: orderId,
        customerId: `cust_${Math.floor(Math.random() * 1000)}`,
        productId: `prod_${Math.floor(Math.random() * 100)}`,
        quantity: Math.floor(Math.random() * 5) + 1,
        price: (Math.random() * 100 + 10).toFixed(2),
    });
    
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`,
            'X-Event-Id': eventId,
        },
    };
    
    const res = http.post(`${BASE_URL}/api/v1/events/orders`, payload, params);
    
    check(res, {
        'Order accepted': (r) => r.status === 202,
    });
    
    sleep(0.1); // pacing interval
}
