import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";
import { SharedArray } from "k6/data";
import { randomIntBetween, randomItem } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const USER_EMAIL = __ENV.USER_EMAIL;
const USER_PASSWORD = __ENV.USER_PASSWORD;
const USERS_CSV = __ENV.USERS_CSV;

const ACADEMY_ID = __ENV.ACADEMY_ID ? Number(__ENV.ACADEMY_ID) : null;
const CLASS_ID = __ENV.CLASS_ID ? Number(__ENV.CLASS_ID) : null;
const SEED_STUDENTS = __ENV.SEED_STUDENTS ? Number(__ENV.SEED_STUDENTS) : 50;
const ENABLE_FCM = (__ENV.ENABLE_FCM || "false").toLowerCase() === "true";
const FCM_STUDENTS = __ENV.FCM_STUDENTS ? Number(__ENV.FCM_STUDENTS) : 20;
const RUN_ID = __ENV.RUN_ID || String(Date.now());
const ENABLE_ATTENDANCE_WRITE = (__ENV.ENABLE_ATTENDANCE_WRITE || "false").toLowerCase() === "true";
const ENABLE_RECEIPT_WRITE = (__ENV.ENABLE_RECEIPT_WRITE || "true").toLowerCase() === "true";
const RECEIPT_NEW_STUDENT = (__ENV.RECEIPT_NEW_STUDENT || "false").toLowerCase() === "true";
const ATTENDANCE_DATE = __ENV.ATTENDANCE_DATE || "2026-02-09";
const ATTENDANCE_TIME = __ENV.ATTENDANCE_TIME || "14:00:00";
const RECEIPT_YEAR = __ENV.RECEIPT_YEAR ? Number(__ENV.RECEIPT_YEAR) : 2026;
const RATE = __ENV.RATE ? Number(__ENV.RATE) : 10;
const DURATION = __ENV.DURATION || "10m";
const ACADEMY_NAME_PREFIX = __ENV.ACADEMY_NAME_PREFIX || "PerfAcademy";
const ACADEMY_BASE_ADDRESS = __ENV.ACADEMY_BASE_ADDRESS || "Seoul";
const ACADEMY_PHONE_NUMBER = __ENV.ACADEMY_PHONE_NUMBER || "01000000000";
const ENABLE_ACADEMY_CREATE =
  (__ENV.ENABLE_ACADEMY_CREATE || (USERS_CSV ? "true" : "false")).toLowerCase() === "true";
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL;
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD;
const ENABLE_ACADEMY_AUTO_APPROVE =
  (__ENV.ENABLE_ACADEMY_AUTO_APPROVE || (ADMIN_EMAIL && ADMIN_PASSWORD ? "true" : "false")).toLowerCase() === "true";

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

const USERS = USERS_CSV
  ? new SharedArray("users", function () {
      return parseUsersCsv(open(USERS_CSV));
    })
  : [];

export const options = {
  scenarios: {
    mixed: {
      executor: "constant-arrival-rate",
      rate: RATE,
      timeUnit: "1s",
      duration: DURATION,
      preAllocatedVUs: 20,
      maxVUs: 100,
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500", "p(99)<1000"],
  },
};

const httpFail4xx = new Counter("http_fail_4xx");
const httpFail5xx = new Counter("http_fail_5xx");
const httpFailNet = new Counter("http_fail_network");
let loggedFailures = 0;
const vuSessions = {};
let adminTokenCache = null;

function authHeaders(token) {
  return {
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    timeout: "120s",
  };
}

function trackFailure(res, label) {
  if (!res) {
    httpFailNet.add(1, { label });
    return;
  }
  if (res.status === 0) {
    httpFailNet.add(1, { label });
  } else if (res.status >= 400 && res.status < 500) {
    httpFail4xx.add(1, { label, status: String(res.status) });
  } else if (res.status >= 500) {
    httpFail5xx.add(1, { label, status: String(res.status) });
  }
}

function request(method, url, body, params, label) {
  let res;
  if (method === "GET") {
    res = http.get(url, params);
  } else if (method === "POST") {
    res = http.post(url, body, params);
  } else if (method === "PUT") {
    res = http.put(url, body, params);
  } else if (method === "PATCH") {
    res = http.patch(url, body, params);
  } else {
    res = http.request(method, url, body, params);
  }
  if (res.status >= 400 || res.status === 0) {
    trackFailure(res, label);
    if (res.status >= 400 && loggedFailures < 20) {
      loggedFailures += 1;
      console.log(
        `FAIL ${label} status=${res.status} url=${url} body=${res.body}`
      );
    }
  }
  return res;
}

function makePhone() {
  return `010${String(randomIntBetween(10000000, 99999999))}`;
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

function login(email, password) {
  const res = request(
    "POST",
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { "Content-Type": "application/json" } },
    "auth.login"
  );
  const ok = check(res, { "login ok": (r) => r.status === 200 });
  if (!ok) {
    throw new Error(`login failed: status=${res.status}, body=${res.body}`);
  }
  return res.json();
}

function getAdminToken() {
  if (adminTokenCache) return adminTokenCache;
  if (!ADMIN_EMAIL || !ADMIN_PASSWORD) {
    throw new Error("ADMIN_EMAIL/ADMIN_PASSWORD required for auto-approve");
  }
  const res = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  adminTokenCache = res.accessToken;
  return adminTokenCache;
}

function approveAcademy(academyId) {
  if (!ENABLE_ACADEMY_AUTO_APPROVE) return;
  const token = getAdminToken();
  const res = request(
    "POST",
    `${BASE_URL}/api/admin/academy/${academyId}/approve`,
    null,
    { headers: { Authorization: `Bearer ${token}` }, timeout: "120s" },
    "academy.approve"
  );
  if (res.status !== 200) {
    throw new Error(`academy approve failed: status=${res.status}, body=${res.body}`);
  }
}

function createAcademy(token, label) {
  const name = `${ACADEMY_NAME_PREFIX}_${label}_${RUN_ID}`;
  const fields = {
    name,
    baseAddress: ACADEMY_BASE_ADDRESS,
    phoneNumber: ACADEMY_PHONE_NUMBER,
  };
  const multipart = buildMultipart(fields);
  const res = request(
    "POST",
    `${BASE_URL}/api/academy/registrations`,
    multipart.body,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": `multipart/form-data; boundary=${multipart.boundary}`,
      },
      timeout: "120s",
    },
    "academy.register"
  );
  if (res.status !== 201 && res.status !== 200) {
    throw new Error(`academy register failed: status=${res.status}, body=${res.body}`);
  }
  const academyId = res.json();
  if (academyId) {
    approveAcademy(academyId);
  }
  return academyId;
}

function getAcademyId(loginRes, token, label) {
  if (ACADEMY_ID) return ACADEMY_ID;
  if (loginRes.academyId) return loginRes.academyId;
  if (loginRes.academies && loginRes.academies.length > 0) return loginRes.academies[0].academyId;
  if (!ENABLE_ACADEMY_CREATE) {
    throw new Error("academyId not found in login response; set ACADEMY_ID or enable ENABLE_ACADEMY_CREATE");
  }
  if (!token) {
    throw new Error("academyId not found and token missing for academy create");
  }
  return createAcademy(token, label || "user");
}

function getClassId(token, academyId) {
  if (CLASS_ID) return CLASS_ID;

  let res = request("GET", `${BASE_URL}/api/academy/${academyId}/class`, null, authHeaders(token), "class.list");
  check(res, { "class list ok": (r) => r.status === 200 });

  const classes = res.json();
  if (classes && classes.length > 0) return classes[0].classId;

  const ages = http.get(`${BASE_URL}/api/ages`, authHeaders(token)).json();
  const subjects = http.get(`${BASE_URL}/api/subjects`, authHeaders(token)).json();

  const ageId = ages[0].id;
  const subjectId = subjects[0].id;

  const body = {
    className: "PerfTest-Class",
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

function createStudentWithParent(token, academyId, classId, i) {
  for (let attempt = 0; attempt < 5; attempt++) {
    const parentPhone = makePhone();
    const body = {
      student: {
        studentName: `perf_student_${RUN_ID}_${i}_${attempt}`,
        studentPhone: makePhone(),
      },
      parent: {
        parentName: `perf_parent_${RUN_ID}_${i}_${attempt}`,
        cardNum: "4111111111111111",
        parentRelationship: "MOTHER",
        parentPhone: parentPhone,
        isPay: true,
        alarm: true,
      },
      classId: classId,
    };
    const res = request(
      "POST",
      `${BASE_URL}/api/academy/${academyId}/student/with-parent`,
      JSON.stringify(body),
      authHeaders(token),
      "student.with_parent"
    );
    if (res.status === 200) {
      return { studentId: res.json().studentId, parentPhone };
    }
    if (res.status === 500 && res.body && res.body.includes("Duplicate entry")) {
      continue;
    }
    return null;
  }
  return null;
}

function parentLogin(parentPhone) {
  const res = request(
    "POST",
    `${BASE_URL}/api/parent/auth/login`,
    JSON.stringify({ parentPhone }),
    { headers: { "Content-Type": "application/json" }, timeout: "120s" },
    "parent.login"
  );
  if (res.status !== 200) {
    return null;
  }
  return res.json();
}

function registerFcmToken(parentId, tokenValue) {
  const url = `${BASE_URL}/api/fcm/register?parentId=${parentId}&fcmToken=${tokenValue}`;
  return request("POST", url, null, { timeout: "120s" }, "fcm.register");
}

function createReceipt(token, studentId, classId, iteration) {
  const month = ((Number(RUN_ID.replace(/\D/g, "").slice(-6)) + iteration) % 12) + 1;
  const mm = String(month).padStart(2, "0");
  const body = { amount: 150000, classId: classId, receiptDate: `${RECEIPT_YEAR}-${mm}-04T10:00:00` };
  return request("POST", `${BASE_URL}/api/student/${studentId}/receipt`, JSON.stringify(body), authHeaders(token), "write.receipt.create");
}

export function setup() {
  if (USERS.length > 0) {
    return {};
  }

  const seedUser = { email: USER_EMAIL, password: USER_PASSWORD };
  if (!seedUser.email || !seedUser.password) {
    throw new Error("USER_EMAIL/USER_PASSWORD required (or provide USERS_CSV)");
  }

  const loginRes = login(seedUser.email, seedUser.password);
  const token = loginRes.accessToken;
  const academyId = getAcademyId(loginRes, token, "seed");
  const classId = getClassId(token, academyId);
  const students = [];

  for (let i = 0; i < SEED_STUDENTS; i++) {
    const created = createStudentWithParent(token, academyId, classId, i);
    if (created) {
      students.push(created);
    }
  }

  let studentIds = students.map((s) => s.studentId);
  const fcmStudentIds = [];

  if (ENABLE_FCM) {
    const limit = Math.min(FCM_STUDENTS, students.length);
    for (let i = 0; i < limit; i++) {
      const created = students[i];
      const parentLoginRes = parentLogin(created.parentPhone);
      if (!parentLoginRes) continue;
      registerFcmToken(parentLoginRes.parentId, `fcm_token_${created.parentPhone}`);
      fcmStudentIds.push(created.studentId);
    }
  }

  if (studentIds.length === 0) {
    const existing = request(
      "GET",
      `${BASE_URL}/api/academy/${academyId}/student`,
      null,
      authHeaders(token),
      "student.list"
    );
    if (existing.status === 200) {
      const list = existing.json();
      if (list && list.length > 0) {
        studentIds = list.map((s) => s.studentId).filter((id) => id != null);
      }
    }
  }

  if (studentIds.length === 0) {
    throw new Error("no studentIds available for test");
  }

  return { token, academyId, classId, studentIds, fcmStudentIds };
}

function pickStudent(studentIds) {
  return randomItem(studentIds);
}

function getVuContext(defaultData) {
  if (!USERS || USERS.length === 0) {
    return defaultData;
  }

  const vu = __VU;
  if (vuSessions[vu]) return vuSessions[vu];

  const user = USERS[(vu - 1) % USERS.length];
  const loginRes = login(user.email, user.password);
  const token = loginRes.accessToken;
  const label = user.email ? user.email.split("@")[0].replace(/[^a-zA-Z0-9_-]/g, "") : `vu${vu}`;
  const academyId = getAcademyId(loginRes, token, label);
  const classId = getClassId(token, academyId);

  let studentIds = [];
  const existing = request(
    "GET",
    `${BASE_URL}/api/academy/${academyId}/student`,
    null,
    authHeaders(token),
    "student.list"
  );
  if (existing.status === 200) {
    const list = existing.json();
    if (list && list.length > 0) {
      studentIds = list.map((s) => s.studentId).filter((id) => id != null);
    }
  }

  if (studentIds.length === 0 && SEED_STUDENTS > 0) {
    for (let i = 0; i < SEED_STUDENTS; i++) {
      const created = createStudentWithParent(token, academyId, classId, i);
      if (created && created.studentId) {
        studentIds.push(created.studentId);
      }
    }
  }

  if (studentIds.length === 0) {
    throw new Error("no studentIds available for test (per-user)");
  }

  const fcmStudentIds = [];
  if (ENABLE_FCM) {
    const limit = Math.min(FCM_STUDENTS, studentIds.length);
    for (let i = 0; i < limit; i++) {
      const created = createStudentWithParent(token, academyId, classId, i);
      if (!created) continue;
      const parentLoginRes = parentLogin(created.parentPhone);
      if (!parentLoginRes) continue;
      registerFcmToken(parentLoginRes.parentId, `fcm_token_${created.parentPhone}`);
      fcmStudentIds.push(created.studentId);
    }
  }

  vuSessions[vu] = { token, academyId, classId, studentIds, fcmStudentIds };
  return vuSessions[vu];
}

export default function (data) {
  const seedData = data || {};
  const ctx = getVuContext(seedData);
  const { token, academyId, classId, studentIds, fcmStudentIds } = ctx;

  const dice = Math.random();

  if (dice < 0.8) {
    const sId = pickStudent(studentIds);
    const year = 2026;
    const month = 2;
    const date = "2026-02-04";

    const readPick = Math.random();
    if (readPick < 0.35) {
      request("GET", `${BASE_URL}/api/attendance/academy/${academyId}/today`, null, authHeaders(token), "read.attendance.today");
    } else if (readPick < 0.55) {
      request("GET", `${BASE_URL}/api/attendance/academy/${academyId}/summary?date=${date}`, null, authHeaders(token), "read.attendance.summary");
    } else if (readPick < 0.7) {
      request("GET", `${BASE_URL}/api/attendance/class/${classId}/monthly-stats?year=${year}&month=${month}`, null, authHeaders(token), "read.attendance.monthly");
    } else if (readPick < 0.85) {
      request("GET", `${BASE_URL}/api/student/${sId}/receipt/month?year=${year}&month=${month}`, null, authHeaders(token), "read.receipt.monthly");
    } else {
      request("GET", `${BASE_URL}/api/academy/${academyId}/dashboard/receipt-main?year=${year}&month=${month}`, null, authHeaders(token), "read.receipt.dashboard");
    }
  } else {
    const useFcmStudent = ENABLE_FCM && fcmStudentIds && fcmStudentIds.length > 0;
    const sId = useFcmStudent ? pickStudent(fcmStudentIds) : pickStudent(studentIds);
    const writePick = Math.random();

    if (writePick < 0.4) {
      const body = {
        studentName: `perf_student_new_${__VU}_${__ITER}`,
        studentPhone: `010${randomIntBetween(10000000, 99999999)}`,
      };
      request("POST", `${BASE_URL}/api/student`, JSON.stringify(body), authHeaders(token), "write.student.create");
    } else if (writePick < 0.7) {
      if (ENABLE_ATTENDANCE_WRITE) {
        const body = { attendanceDate: ATTENDANCE_DATE, checkIn: ATTENDANCE_TIME };
        request("POST", `${BASE_URL}/api/student/${sId}/attendance`, JSON.stringify(body), authHeaders(token), "write.attendance.create");
      }
    } else {
      if (!ENABLE_RECEIPT_WRITE) {
        return;
      }
      if (RECEIPT_NEW_STUDENT) {
        const created = createStudentWithParent(token, academyId, classId, __ITER);
        if (created && created.studentId) {
          createReceipt(token, created.studentId, classId, __ITER);
        }
      } else {
        createReceipt(token, sId, classId, __ITER);
      }
    }
  }

  sleep(0.2);
}
