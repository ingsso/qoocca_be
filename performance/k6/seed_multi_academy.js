import http from "k6/http";
import { check, sleep } from "k6";
import { randomIntBetween } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const USERS_CSV = __ENV.USERS_CSV;
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL;
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD;

const STUDENTS_PER_ACADEMY = __ENV.STUDENTS_PER_ACADEMY ? Number(__ENV.STUDENTS_PER_ACADEMY) : 20;
const CLASS_NAME_PREFIX = __ENV.CLASS_NAME_PREFIX || "PerfClass";
const ACADEMY_NAME_PREFIX = __ENV.ACADEMY_NAME_PREFIX || "PerfAcademy";
const ACADEMY_BASE_ADDRESS = __ENV.ACADEMY_BASE_ADDRESS || "Seoul";
const ACADEMY_PHONE_NUMBER = __ENV.ACADEMY_PHONE_NUMBER || "01000000000";
const RUN_ID = __ENV.RUN_ID || String(Date.now());

export const options = {
  vus: 1,
  iterations: 1,
};

function parseUsersCsv(contents) {
  if (!contents) return [];
  const lines = contents.split(/\r?\n/).filter((line) => line.trim().length > 0);
  if (lines.length <= 1) return [];
  const users = [];
  for (let i = 1; i < lines.length; i++) {
    const [email, password] = lines[i].split(",").map((v) => v.trim());
    if (email && password) users.push({ email, password });
  }
  return users;
}

function buildMultipart(fields) {
  const boundary = `----k6boundary${Date.now()}${Math.random().toString(16).slice(2)}`;
  let body = "";
  Object.keys(fields).forEach((key) => {
    const value = fields[key];
    if (value === undefined || value === null) return;
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="${key}"\r\n\r\n`;
    body += `${value}\r\n`;
  });
  body += `--${boundary}--\r\n`;
  return { body, boundary };
}

function authHeaders(token) {
  return {
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    timeout: "120s",
  };
}

function login(email, password) {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { "Content-Type": "application/json" }, timeout: "120s" }
  );
  const ok = check(res, { "login ok": (r) => r.status === 200 });
  if (!ok) {
    throw new Error(`login failed: status=${res.status}, body=${res.body}`);
  }
  return res.json();
}

function getAdminToken() {
  if (!ADMIN_EMAIL || !ADMIN_PASSWORD) {
    throw new Error("ADMIN_EMAIL/ADMIN_PASSWORD required");
  }
  const res = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  return res.accessToken;
}

function approveAcademy(adminToken, academyId) {
  const res = http.post(
    `${BASE_URL}/api/admin/academy/${academyId}/approve`,
    null,
    { headers: { Authorization: `Bearer ${adminToken}` }, timeout: "120s" }
  );
  check(res, { "academy approve ok": (r) => r.status === 200 });
}

function createAcademy(token, label) {
  const fields = {
    name: `${ACADEMY_NAME_PREFIX}_${label}_${RUN_ID}`,
    baseAddress: ACADEMY_BASE_ADDRESS,
    phoneNumber: ACADEMY_PHONE_NUMBER,
  };
  const multipart = buildMultipart(fields);
  const res = http.post(
    `${BASE_URL}/api/academy/registrations`,
    multipart.body,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": `multipart/form-data; boundary=${multipart.boundary}`,
      },
      timeout: "120s",
    }
  );
  check(res, { "academy create ok": (r) => r.status === 201 || r.status === 200 });
  return res.json();
}

function getAcademyId(loginRes) {
  if (loginRes.academyId) return loginRes.academyId;
  if (loginRes.academies && loginRes.academies.length > 0) return loginRes.academies[0].academyId;
  return null;
}

function getClassId(token, academyId, label) {
  let res = http.get(`${BASE_URL}/api/academy/${academyId}/class`, authHeaders(token));
  if (res.status === 200) {
    const classes = res.json();
    if (classes && classes.length > 0) return classes[0].classId;
  }

  const ages = http.get(`${BASE_URL}/api/ages`, authHeaders(token)).json();
  const subjects = http.get(`${BASE_URL}/api/subjects`, authHeaders(token)).json();
  const ageId = ages[0].id;
  const subjectId = subjects[0].id;

  const body = {
    className: `${CLASS_NAME_PREFIX}_${label}`,
    startTime: "14:00",
    endTime: "16:00",
    monday: true,
    tuesday: false,
    wednesday: false,
    thursday: false,
    friday: true,
    saturday: false,
    sunday: false,
    price: 600000,
    ageId,
    subjectId,
  };

  res = http.post(`${BASE_URL}/api/academy/${academyId}/class`, JSON.stringify(body), authHeaders(token));
  check(res, { "class create ok": (r) => r.status === 200 });
  return res.json().classId;
}

function makePhone() {
  return `010${String(randomIntBetween(10000000, 99999999))}`;
}

function createStudentWithParent(token, academyId, classId, i) {
  for (let attempt = 0; attempt < 5; attempt++) {
    const parentPhone = makePhone();
    const body = {
      student: {
        studentName: `seed_student_${academyId}_${i}_${attempt}`,
        studentPhone: makePhone(),
      },
      parent: {
        parentName: `seed_parent_${academyId}_${i}_${attempt}`,
        cardNum: "4111111111111111",
        parentRelationship: "MOTHER",
        parentPhone,
        isPay: true,
        alarm: true,
      },
      classId,
    };
    const res = http.post(
      `${BASE_URL}/api/academy/${academyId}/student/with-parent`,
      JSON.stringify(body),
      authHeaders(token)
    );
    if (res.status === 200) return true;
    if (res.status === 500 && res.body && res.body.includes("Duplicate entry")) {
      continue;
    }
    return false;
  }
  return false;
}

export default function () {
  if (!USERS_CSV) throw new Error("USERS_CSV required");

  const users = parseUsersCsv(open(USERS_CSV));
  if (users.length === 0) throw new Error("no users found in USERS_CSV");

  const adminToken = getAdminToken();

  for (let i = 0; i < users.length; i++) {
    const user = users[i];
    const label = user.email.split("@")[0].replace(/[^a-zA-Z0-9_-]/g, "");

    const loginRes = login(user.email, user.password);
    const token = loginRes.accessToken;

    let academyId = getAcademyId(loginRes);
    if (!academyId) {
      academyId = createAcademy(token, label);
      approveAcademy(adminToken, academyId);
      sleep(0.2);
    }

    const classId = getClassId(token, academyId, label);
    for (let s = 0; s < STUDENTS_PER_ACADEMY; s++) {
      createStudentWithParent(token, academyId, classId, s);
    }

    sleep(0.1);
  }
}
