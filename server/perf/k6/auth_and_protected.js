import http from "k6/http";
import { check, sleep, group, fail } from "k6";

// -------------------- ENV --------------------
const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";
const EMAIL = __ENV.EMAIL || "perf_user@test.com";
const PASSWORD = __ENV.PASSWORD || "Password123!";
const LOGIN_PATH = __ENV.LOGIN_PATH || "/api/auth/login";
const PROTECTED_PATH = __ENV.PROTECTED_PATH || "/api/posts/getall";

// smoke | load | stress | spike | soak
const TEST_TYPE = (__ENV.TEST_TYPE || "load").toLowerCase();

// -------------------- OPTIONS (by TEST_TYPE) --------------------
function optionsByType(type) {
    switch (type) {
        case "smoke":
            return {
                scenarios: {
                    smoke: {
                        executor: "constant-vus",
                        vus: 2,
                        duration: "30s",
                        gracefulStop: "5s",
                    },
                },
            };

        case "stress":
            return {
                scenarios: {
                    stress: {
                        executor: "ramping-vus",
                        startVUs: 0,
                        stages: [
                            { duration: "20s", target: 20 },
                            { duration: "40s", target: 100 },
                            { duration: "30s", target: 200 },
                            { duration: "20s", target: 0 },
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
                            { duration: "20s", target: 200 },
                            { duration: "10s", target: 0 },
                        ],
                        gracefulRampDown: "10s",
                    },
                },
            };

        case "soak":
            return {
                scenarios: {
                    soak: {
                        executor: "constant-vus",
                        vus: 50,
                        duration: "10m",
                        gracefulStop: "10s",
                    },
                },
            };

        case "load":
        default:
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
    }
}

export const options = {
    ...optionsByType(TEST_TYPE),

    thresholds: {
        http_req_failed: ["rate<0.05"],

        // окремо по ендпоінтах (через tags)
        "http_req_duration{endpoint:login}": ["p(95)<800"],
        "http_req_duration{endpoint:protected}": ["p(95)<500"],

        "http_req_failed{endpoint:protected}": ["rate<0.02"],
    },
};

function randomBetween(min, max) {
    return Math.random() * (max - min) + min;
}

function extractJwt(res) {
    const body = (res.body ?? "").trim();

    const ct = String(res.headers["Content-Type"] || res.headers["content-type"] || "").toLowerCase();
    const looksJson = body.startsWith("{") || body.startsWith("[");
    if (ct.includes("application/json") || looksJson) {
        try {
            const obj = JSON.parse(body);
            const candidate =
                obj?.token || obj?.jwt || obj?.accessToken || obj?.access_token || obj?.data?.token || null;
            if (typeof candidate === "string" && candidate.split(".").length === 3) return candidate;
        } catch (_) {}
    }

    const unquoted =
        (body.startsWith('"') && body.endsWith('"')) || (body.startsWith("'") && body.endsWith("'"))
            ? body.slice(1, -1)
            : body;

    if (unquoted.split(".").length === 3) return unquoted;

    return null;
}

function loginAndGetToken() {
    const payload = JSON.stringify({ email: EMAIL, password: PASSWORD });

    const res = http.post(`${BASE_URL}${LOGIN_PATH}`, payload, {
        headers: { "Content-Type": "application/json" },
        tags: { endpoint: "login" },
        timeout: "15s",
    });

    const ok = check(res, {
        "login: status 200": (r) => r.status === 200,
    });

    if (!ok) {
        console.log(`LOGIN FAIL status=${res.status} body=${String(res.body).slice(0, 200)}`);
        return null;
    }

    const token = extractJwt(res);
    check(res, {
        "token extracted": () => !!token,
    });

    if (!token) {
        console.log(`LOGIN OK but token not extracted. CT=${res.headers["Content-Type"]} body=${String(res.body).slice(0, 200)}`);
    }

    return token;
}

// -------------------- VU CACHE --------------------
let cachedToken = null;

// -------------------- TEST --------------------
export default function () {
    group("Auth", () => {
        // 1 раз на VU
        if (__ITER === 0 || !cachedToken) {
            cachedToken = loginAndGetToken();
            if (!cachedToken) fail("Cannot obtain JWT token");
        }
    });

    group("Protected endpoint", () => {
        const res = http.get(`${BASE_URL}${PROTECTED_PATH}`, {
            headers: { Authorization: `Bearer ${cachedToken}` },
            tags: { endpoint: "protected" },
            timeout: "15s",
        });

        const ok = check(res, {
            "protected: status 200": (r) => r.status === 200,
            "protected: json": (r) => String(r.headers["Content-Type"] || "").includes("application/json"),
            "protected: non-empty body": (r) => (r.body || "").length > 0,
        });

        if (!ok && __ITER < 3) {
            console.log(`PROTECTED FAIL status=${res.status} body=${String(res.body).slice(0, 250)}`);
        }
    });

    sleep(randomBetween(0.5, 2.5));
}
