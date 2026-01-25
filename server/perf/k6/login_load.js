import http from "k6/http";
import { check, sleep, group } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";
const EMAIL = __ENV.EMAIL || "perf_user@test.com";
const PASSWORD = __ENV.PASSWORD || "Password123!";
const LOGIN_PATH = __ENV.LOGIN_PATH || "/api/auth/login";

const TEST_TYPE = (__ENV.TEST_TYPE || "stress").toLowerCase();

function optionsByType(type) {
    switch (type) {
        case "smoke":
            return {
                scenarios: {
                    smoke: { executor: "constant-vus", vus: 2, duration: "20s" },
                },
            };

        case "load":
            return {
                scenarios: {
                    load: {
                        executor: "ramping-vus",
                        startVUs: 0,
                        stages: [
                            { duration: "10s", target: 10 },
                            { duration: "30s", target: 50 },
                            { duration: "20s", target: 100 },
                            { duration: "10s", target: 0 },
                        ],
                        gracefulRampDown: "10s",
                    },
                },
            };

        case "spike":
            return {
                scenarios: {
                    spike: {
                        executor: "ramping-vus",
                        startVUs: 0,
                        stages: [
                            { duration: "10s", target: 10 },
                            { duration: "5s", target: 200 },
                            { duration: "15s", target: 200 },
                            { duration: "10s", target: 0 },
                        ],
                        gracefulRampDown: "10s",
                    },
                },
            };

        case "stress":
        default:
            return {
                scenarios: {
                    stress: {
                        executor: "ramping-vus",
                        startVUs: 0,
                        stages: [
                            { duration: "15s", target: 20 },
                            { duration: "30s", target: 100 },
                            { duration: "25s", target: 200 },
                            { duration: "20s", target: 0 },
                        ],
                        gracefulRampDown: "10s",
                    },
                },
            };
    }
}

export const options = {
    ...optionsByType(TEST_TYPE),
    thresholds: {
        http_req_failed: ["rate<0.05"],
        "http_req_duration{endpoint:login}": ["p(95)<900"],
    },
};

function extractJwtPlainOrJson(res) {
    const body = (res.body ?? "").trim();

    // JSON token
    try {
        const obj = JSON.parse(body);
        const token = obj?.token || obj?.jwt || obj?.accessToken || obj?.access_token || obj?.data?.token || null;
        if (typeof token === "string") return token;
    } catch (_) {}

    // plaintext jwt
    const unquoted =
        (body.startsWith('"') && body.endsWith('"')) || (body.startsWith("'") && body.endsWith("'"))
            ? body.slice(1, -1)
            : body;

    return unquoted || null;
}

export default function () {
    group("Login (each iteration)", () => {
        const payload = JSON.stringify({ email: EMAIL, password: PASSWORD });

        const res = http.post(`${BASE_URL}${LOGIN_PATH}`, payload, {
            headers: { "Content-Type": "application/json" },
            tags: { endpoint: "login" },
            timeout: "15s",
        });

        const ok = check(res, {
            "login: status 200": (r) => r.status === 200,
            "login: token not empty": (r) => {
                const t = extractJwtPlainOrJson(r);
                return !!t && t.length > 20;
            },
        });

        if (!ok && __ITER < 3) {
            console.log(`LOGIN FAIL status=${res.status} body=${String(res.body).slice(0, 200)}`);
        }
    });

    sleep(0.5);
}
