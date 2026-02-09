import http from "k6/http";
import { check, sleep } from "k6";
import { randomIntBetween } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const USER_EMAIL = __ENV.USER_EMAIL;
const USER_PASSWORD = __ENV.USER_PASSWORD;

const ACADEMY_ID = __ENV.ACADEMY_ID ? Number(__ENV.ACADEMY_ID) : null;
const CLASS_ID = __ENV.CLASS_ID ? Number(__ENV.CLASS_ID) : null;

const STUDENTS = __ENV.STUDENTS ? Number(__ENV.STUDENTS) : 200;
const ATTENDANCE_DAYS = __ENV.ATTENDANCE_DAYS ? Number(__ENV.ATTENDANCE_DAYS) : 5;
const RECEIPTS_PER_STUDENT = __ENV.RECEIPTS_PER_STUDENT ? Number(__ENV.RECEIPTS_PER_STUDENT) : 2;
const ENABLE_ATTENDANCE_SEED = (__ENV.ENABLE_ATTENDANCE_SEED || "true").toLowerCase() === "true";
const ENABLE_RECEIPT_SEED = (__ENV.ENABLE_RECEIPT_SEED || "true").toLowerCase() === "true";

function authHeaders(token) {
  return { headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" } };
}

function login() {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD }),
    { headers: { "Content-Type": "application/json" } }
  );
  const ok = check(res, { "login ok": (r) => r.status === 200 });
  if (!ok) {
    throw new Error(`login failed: status=${res.status}, body=${res.body}`);
  }
  return res.json();
}

function getAcademyId(loginRes) {
  if (ACADEMY_ID) return ACADEMY_ID;
  if (loginRes.academyId) return loginRes.academyId;
  if (loginRes.academies && loginRes.academies.length > 0) return loginRes.academies[0].academyId;
  throw new Error("academyId not found in login response; set ACADEMY_ID env or check login account has academy");
}

function getClassId(token, academyId) {
  if (CLASS_ID) return CLASS_ID;

  let res = http.get(`${BASE_URL}/api/academy/${academyId}/class`, authHeaders(token));
  check(res, { "class list ok": (r) => r.status === 200 });

  const classes = res.json();
  if (classes && classes.length > 0) return classes[0].classId;

  const ages = http.get(`${BASE_URL}/api/ages`, authHeaders(token)).json();
  const subjects = http.get(`${BASE_URL}/api/subjects`, authHeaders(token)).json();

  const ageId = ages[0].id;
  const subjectId = subjects[0].id;

  const body = {
    className: "Seed-Class",
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

function createStudent(token, academyId, i) {
  const body = {
    studentName: `seed_student_${i}`,
    studentPhone: `010${randomIntBetween(10000000, 99999999)}`,
  };
  const res = http.post(`${BASE_URL}/api/academy/${academyId}/student`, JSON.stringify(body), authHeaders(token));
  if (res.status !== 200) return null;
  return res.json().studentId;
}

function registerStudentToClass(token, academyId, classId, studentId) {
  const body = { studentId };
  http.post(`${BASE_URL}/api/academy/${academyId}/class/${classId}/student`, JSON.stringify(body), authHeaders(token));
}

function createAttendance(token, studentId, date) {
  const body = { attendanceDate: date, checkIn: "09:05:00" };
  http.post(`${BASE_URL}/api/student/${studentId}/attendance`, JSON.stringify(body), authHeaders(token));
}

function createReceipt(token, studentId, classId, dateTime) {
  const body = { amount: 150000, classId: classId, receiptDate: dateTime };
  http.post(`${BASE_URL}/api/student/${studentId}/receipt`, JSON.stringify(body), authHeaders(token));
}

export default function () {
  if (!USER_EMAIL || !USER_PASSWORD) throw new Error("USER_EMAIL/USER_PASSWORD required");

  const loginRes = login();
  const token = loginRes.accessToken;
  const academyId = getAcademyId(loginRes);
  const classId = getClassId(token, academyId);

  const studentIds = [];
  for (let i = 0; i < STUDENTS; i++) {
    const id = createStudent(token, academyId, i);
    if (id) {
      studentIds.push(id);
      registerStudentToClass(token, academyId, classId, id);
    }
  }

  for (const studentId of studentIds) {
    if (ENABLE_ATTENDANCE_SEED) {
      for (let d = 0; d < ATTENDANCE_DAYS; d++) {
        const day = String(1 + d).padStart(2, "0");
        const date = `2026-02-${day}`;
        createAttendance(token, studentId, date);
      }
    }

    if (ENABLE_RECEIPT_SEED) {
      for (let r = 0; r < RECEIPTS_PER_STUDENT; r++) {
        const day = String(1 + r).padStart(2, "0");
        const dateTime = `2026-02-${day}T10:00:00`;
        createReceipt(token, studentId, classId, dateTime);
      }
    }
  }

  sleep(1);
}
