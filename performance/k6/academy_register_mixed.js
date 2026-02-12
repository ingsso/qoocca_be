import http from "k6/http";
import { check, sleep } from "k6";

const TOTAL_VUS = __ENV.VUS ? parseInt(__ENV.VUS, 10) : 10;
const DURATION = __ENV.DURATION || "3m";
const FULL_VU_RATIO = __ENV.FULL_VU_RATIO
  ? parseFloat(__ENV.FULL_VU_RATIO)
  : 0.2;

const FULL_VUS = Math.max(1, Math.round(TOTAL_VUS * FULL_VU_RATIO));
const REGISTER_ONLY_VUS = Math.max(1, TOTAL_VUS - FULL_VUS);

export const options = {
  scenarios: {
    full_flow: {
      executor: "constant-vus",
      vus: FULL_VUS,
      duration: DURATION,
      exec: "fullFlow",
    },
    register_only: {
      executor: "constant-vus",
      vus: REGISTER_ONLY_VUS,
      duration: DURATION,
      exec: "registerOnly",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1500"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

const CERT_PATH = __ENV.CERT_PATH || "/scripts/bebi1.jpg";
const IMAGE1_PATH = __ENV.IMAGE1_PATH || "/scripts/bebi2.jpg";
const IMAGE2_PATH = __ENV.IMAGE2_PATH || "/scripts/mon.jpg";
const ACADEMIES_PER_USER = __ENV.ACADEMIES_PER_USER
  ? parseInt(__ENV.ACADEMIES_PER_USER, 10)
  : 2;
const RUN_ID = __ENV.RUN_ID
  ? parseInt(__ENV.RUN_ID, 10)
  : Math.floor(Date.now() % 100000000);

const CERT_BIN = open(CERT_PATH, "b");
const IMAGE1_BIN = open(IMAGE1_PATH, "b");
const IMAGE2_BIN = open(IMAGE2_PATH, "b");

function uniquePhone(vu, iter) {
  const raw = RUN_ID + vu * 100000 + iter;
  const suffix = String(raw % 100000000).padStart(8, "0");
  return `010${suffix}`;
}

function uniqueUser(vu, iter) {
  const id = vu * 100000 + iter;
  const id4 = String(id % 10000).padStart(4, "0");
  const id8 = String(id % 100000000).padStart(8, "0");
  return {
    username: `user${id8}`,
    email: `user${id4}-${vu}-${iter}@test.com`,
    password: `user${id8}`,
  };
}

function logIfFailed(res, label) {
  if (res.status < 200 || res.status >= 300) {
    console.warn(`${label} failed status=${res.status} body=${res.body}`);
  }
}

function loginAndGetToken(email, password) {
  const headers = { "Content-Type": "application/json" };
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers }
  );
  check(loginRes, { "login 200": (r) => r.status === 200 });
  logIfFailed(loginRes, "login");

  let accessToken = "";
  try {
    accessToken = JSON.parse(loginRes.body).accessToken || "";
  } catch (_) {
    accessToken = "";
  }

  if (!accessToken) {
    console.warn("login missing accessToken");
  }

  return accessToken;
}

function registerAcademies(accessToken, phone, vu, iter) {
  const certFile = http.file(CERT_BIN, "bebi1.jpg", "image/jpeg");
  const image1 = http.file(IMAGE1_BIN, "bebi2.jpg", "image/jpeg");
  const image2 = http.file(IMAGE2_BIN, "mon.jpg", "image/jpeg");

  for (let i = 0; i < ACADEMIES_PER_USER; i += 1) {
    const academyName = `Test Academy ${vu}-${iter}-${i}`;
    const formData = {
      name: academyName,
      baseAddress: "서울특별시 강남구 테헤란로 123",
      phoneNumber: phone,
      ageIds: ["1", "2"],
      subjects: ["70", "73"],
      certificateFile: certFile,
      imageFiles: [image1, image2],
    };

    const academyRes = http.post(
      `${BASE_URL}/api/academy/registrations`,
      formData,
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      }
    );

    check(academyRes, {
      "academy register 201": (r) => r.status === 201,
    });
    logIfFailed(academyRes, "academy register");
  }
}

let cachedToken = "";
let cachedLoginKey = "";

function getAccountForVu(vu) {
  const emailsRaw = (__ENV.LOGIN_EMAILS || "").split(",").map((v) => v.trim()).filter(Boolean);
  const passwordsRaw = (__ENV.LOGIN_PASSWORDS || "")
    .split(",")
    .map((v) => v.trim())
    .filter(Boolean);

  if (emailsRaw.length === 0 || passwordsRaw.length === 0) {
    return { email: "", password: "" };
  }

  const idx = (vu - 1) % Math.min(emailsRaw.length, passwordsRaw.length);
  return { email: emailsRaw[idx], password: passwordsRaw[idx] };
}

export function fullFlow() {
  const vu = __VU;
  const iter = __ITER;

  const phone = uniquePhone(vu, iter);
  const user = uniqueUser(vu, iter);

  const headers = { "Content-Type": "application/json" };

  const sendRes = http.post(
    `${BASE_URL}/api/auth/send-code`,
    JSON.stringify({ phone }),
    { headers }
  );
  check(sendRes, { "send-code 200": (r) => r.status === 200 });
  logIfFailed(sendRes, "send-code");

  const verifyRes = http.post(
    `${BASE_URL}/api/auth/verify-code`,
    JSON.stringify({ phone, code: "123456" }),
    { headers }
  );
  check(verifyRes, { "verify-code 200": (r) => r.status === 200 });
  logIfFailed(verifyRes, "verify-code");

  const signupRes = http.post(
    `${BASE_URL}/api/auth/signup`,
    JSON.stringify({
      username: user.username,
      email: user.email,
      password: user.password,
      phone,
      agreements: {
        service: true,
        privacy: true,
        thirdParty: true,
        marketing: false,
      },
    }),
    { headers }
  );
  check(signupRes, { "signup 200": (r) => r.status === 200 });
  logIfFailed(signupRes, "signup");
  if (signupRes.status !== 200) {
    sleep(1);
    return;
  }

  const accessToken = loginAndGetToken(user.email, user.password);
  if (!accessToken) {
    sleep(1);
    return;
  }

  registerAcademies(accessToken, phone, vu, iter);
  sleep(1);
}

export function registerOnly() {
  const vu = __VU;
  const iter = __ITER;

  const phone = uniquePhone(vu, iter);

  let accessToken = __ENV.ACCESS_TOKEN || "";
  if (!accessToken) {
    const account = getAccountForVu(vu);
    if (!account.email || !account.password) {
      console.warn(
        "register_only missing ACCESS_TOKEN or LOGIN_EMAILS/LOGIN_PASSWORDS"
      );
      sleep(1);
      return;
    }

    const loginKey = `${account.email}:${account.password}`;
    if (cachedToken && cachedLoginKey === loginKey) {
      accessToken = cachedToken;
    } else {
      accessToken = loginAndGetToken(account.email, account.password);
      if (accessToken) {
        cachedToken = accessToken;
        cachedLoginKey = loginKey;
      }
    }
  }

  if (!accessToken) {
    sleep(1);
    return;
  }

  registerAcademies(accessToken, phone, vu, iter);
  sleep(1);
}
