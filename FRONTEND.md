# EduPedu API Documentation

> **Base URL:** `http://136.116.64.6:8080`
> **API Prefix:** `/api/v1`
> **Content-Type:** `application/json`
> **Authentication:** Bearer JWT Token (RSA256 signed)

---

## Table of Contents

1. [URL Structure & Security Model](#1-url-structure--security-model)
2. [Authentication](#2-authentication)
3. [Admin Endpoints `/admin/`](#3-admin-endpoints)
4. [Teacher Endpoints `/teacher/`](#4-teacher-endpoints)
5. [University-Scoped Endpoints `/university/`](#5-university-scoped-endpoints)
6. [Public Read Endpoints](#6-public-read-endpoints)
7. [Student/General Endpoints](#7-studentgeneral-endpoints)
8. [Enums Reference](#8-enums-reference)
9. [Role-Based Access Matrix](#9-role-based-access-matrix)
10. [Error Handling](#10-error-handling)
11. [Test Credentials (Data Seeder)](#11-test-credentials-data-seeder)

---

## 1. URL Structure & Security Model

The API uses a **prefix-based authorization** model. The URL prefix determines access control:

```
/api/v1/auth/**        → Public (no auth required)
/api/v1/admin/**       → ADMIN, UNIVERSITY_ADMIN
/api/v1/teacher/**     → ADMIN, UNIVERSITY_ADMIN, TEACHER
/api/v1/university/**  → ADMIN, UNIVERSITY_ADMIN, TEACHER
/api/v1/**             → Any authenticated user
```

**Authentication Header:**
```
Authorization: Bearer <access_token>
```

---

## 2. Authentication

> **No authentication required** for these endpoints.

### 2.1 Register

```
POST /api/v1/auth/register
```

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "role": "ROLE_STUDENT"
}
```

| Field      | Type   | Required | Description                                                |
|------------|--------|----------|------------------------------------------------------------|
| email      | string | ✅       | Must be unique, valid email                                |
| password   | string | ✅       | User password                                              |
| firstName  | string | ✅       | First name                                                 |
| lastName   | string | ✅       | Last name                                                  |
| role       | string | ❌       | One of the [Role](#roles) enum values                      |

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "User created successfully",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "ROLE_STUDENT"
  }
}
```

---

### 2.2 Login

```
POST /api/v1/auth/login
```

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "refresh_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer"
}
```

> ⚠️ Store both tokens. `access_token` expires in **24 hours**, `refresh_token` in **7 days**.

---

### 2.3 Refresh Token

```
POST /api/v1/auth/refresh
```

**Request Body:**

```json
{
  "refresh_token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

> **Note:** The field name is `refresh_token` (snake_case).

**Response:** `200 OK` — Returns new `access_token` and `refresh_token`.

---

### 2.4 Forgot Password

```
POST /api/v1/auth/forgot-password?email=user@example.com
```

### 2.5 Reset Password

```
POST /api/v1/auth/reset-password?token=<reset_token>&password=NewPass!&confirmPassword=NewPass!
```

---

### Frontend Integration — Auth Interceptor

```javascript
async function fetchWithAuth(url, options = {}) {
  let token = localStorage.getItem('access_token');
  options.headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };

  let response = await fetch(url, options);

  if (response.status === 403 || response.status === 401) {
    const refreshRes = await fetch('/api/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        refresh_token: localStorage.getItem('refresh_token')
      })
    });

    if (refreshRes.ok) {
      const tokens = await refreshRes.json();
      localStorage.setItem('access_token', tokens.access_token);
      localStorage.setItem('refresh_token', tokens.refresh_token);
      options.headers['Authorization'] = `Bearer ${tokens.access_token}`;
      response = await fetch(url, options);
    } else {
      window.location.href = '/login';
    }
  }
  return response;
}
```

---

## 3. Admin Endpoints

> **Requires:** `ROLE_ADMIN` or `ROLE_UNIVERSITY_ADMIN`

### 3.1 Users

| Method | Endpoint                              | Description            | Status |
|--------|---------------------------------------|------------------------|--------|
| GET    | `/api/v1/admin/users`                 | List all users         | 200    |
| GET    | `/api/v1/admin/university`            | Users by university    | 200    |
| GET    | `/api/v1/admin/{id}`                  | Get user by ID         | 200    |
| GET    | `/api/v1/admin/email/{email}`         | Get user by email      | 200    |
| POST   | `/api/v1/admin/users`                 | Create user            | 201    |
| DELETE | `/api/v1/admin/{id}`                  | Delete user            | 204    |

**Query params for `/admin/university`:** `?universityId={id}`

---

### 3.2 Admin Registration

```
POST /api/v1/admin/registerNewUser
```

```json
{
  "email": "teacher@uni.edu",
  "password": "SecurePass123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1234567890",
  "role": "ROLE_TEACHER",
  "university": { "id": 1 },
  "studentGroupId": null,
  "subjects": [{ "id": 1 }, { "id": 2 }]
}
```

---

### 3.3 Students (Admin)

| Method | Endpoint                                       | Description                    | Status |
|--------|-------------------------------------------------|--------------------------------|--------|
| GET    | `/api/v1/admin/students`                       | List all students              | 200    |
| POST   | `/api/v1/admin/students`                       | Create student profile         | 201    |
| PUT    | `/api/v1/admin/students/{id}`                  | Update student                 | 200    |
| DELETE | `/api/v1/admin/students/{id}`                  | Delete student                 | 204    |
| GET    | `/api/v1/admin/students/unassigned`            | Unassigned students            | 200    |
| PUT    | `/api/v1/admin/students/assign/{id}`           | Assign student (TODO)          | 200    |
| PUT    | `/api/v1/admin/students/{studentId}/group`     | Assign to group                | 200    |
| PUT    | `/api/v1/admin/bulk-assign`                    | Bulk assign to group           | 200    |

**Create Student:**
```json
{
  "userId": 10,
  "studentNumber": "STU-2024-001",
  "accountNumber": "ACC-001",
  "parentPhone": "+1234567890",
  "studentGroupId": 1
}
```

**Bulk Assign:**
```json
{
  "studentIds": [1, 2, 3],
  "classGroupId": 2
}
```

**Query params for `unassigned`:** `?universityId={id}`

---

### 3.4 Teachers (Admin)

| Method | Endpoint                                         | Description             | Status |
|--------|--------------------------------------------------|-------------------------|--------|
| GET    | `/api/v1/admin/teachers/{id}`                    | Get teacher by ID       | 200    |
| GET    | `/api/v1/admin/teachers/user/{userId}`           | Get by user ID          | 200    |
| POST   | `/api/v1/admin/teachers`                         | Create teacher          | 201    |
| PUT    | `/api/v1/admin/teachers/{id}`                    | Update teacher          | 200    |
| DELETE | `/api/v1/admin/teachers/{id}`                    | Delete teacher          | 204    |
| GET    | `/api/v1/admin/teachers/getWorkload`             | Teacher workload report | 200    |

**Create Teacher:**
```json
{
  "userId": 5,
  "employeeNumber": "EMP-001",
  "subjectIds": [1, 2]
}
```

**Workload query params:** `?teacherId={id}&year={y}&month={m}`

---

### 3.5 Subjects (Admin)

| Method | Endpoint                            | Description        | Status |
|--------|--------------------------------------|--------------------|--------|
| GET    | `/api/v1/admin/subjects`            | List all subjects  | 200    |
| GET    | `/api/v1/admin/subjects/{id}`       | Get by ID          | 200    |
| POST   | `/api/v1/admin/subjects`            | Create subject     | 201    |
| PUT    | `/api/v1/admin/subjects/{id}`       | Update subject     | 200    |
| DELETE | `/api/v1/admin/subjects/{id}`       | Delete subject     | 204    |

```json
{
  "name": "Linear Algebra",
  "description": "Introductory course",
  "credits": 3
}
```

---

### 3.6 Faculties (Admin)

| Method | Endpoint                            | Description        | Status |
|--------|--------------------------------------|--------------------|--------|
| POST   | `/api/v1/admin/faculties`           | Create faculty     | 201    |
| PUT    | `/api/v1/admin/faculties/{id}`      | Update faculty     | 200    |
| DELETE | `/api/v1/admin/faculties/{id}`      | Delete faculty     | 204    |

---

### 3.7 Student Groups (Admin)

| Method | Endpoint                              | Description          | Status |
|--------|----------------------------------------|----------------------|--------|
| POST   | `/api/v1/admin/student-groups`        | Create group         | 201    |
| PUT    | `/api/v1/admin/student-groups/{id}`   | Update group         | 200    |
| DELETE | `/api/v1/admin/student-groups/{id}`   | Delete group         | 204    |

---

### 3.8 Schedule (Admin)

| Method | Endpoint                             | Description                | Status |
|--------|---------------------------------------|----------------------------|--------|
| POST   | `/api/v1/admin/schedule`             | Create schedule entry      | 200    |
| POST   | `/api/v1/admin/schedule/generate`    | Auto-generate schedule     | 200    |
| DELETE | `/api/v1/admin/schedule/{id}`        | Delete schedule entry      | 204    |

**Create Schedule:**
```json
{
  "studentGroupId": 1,
  "classId": 3,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "endTime": "10:30",
  "room": "Building A, Room 101",
  "lessonNumber": 1
}
```

**Auto-Generate:**
```json
{
  "dayStartTime": "08:00",
  "dayEndTime": "17:00",
  "lessonDurationMinutes": 90,
  "breakDurationMinutes": 15,
  "classMappings": [
    { "classId": 1, "studentGroupIds": [1, 2] }
  ]
}
```

---

### 3.9 Announcements (Admin)

| Method | Endpoint                              | Description             | Status |
|--------|----------------------------------------|-------------------------|--------|
| POST   | `/api/v1/admin/announcements`         | Create announcement     | 200    |
| DELETE | `/api/v1/admin/announcements/{id}`    | Delete announcement     | 204    |

```json
{
  "title": "Midterm Exam Schedule",
  "content": "Midterm exams April 20-25...",
  "targetRole": "ROLE_STUDENT",
  "targetStudentGroupId": null,
  "important": true,
  "expiresAt": "2026-04-25T23:59:59"
}
```

---

## 4. Teacher Endpoints

> **Requires:** `ROLE_ADMIN`, `ROLE_UNIVERSITY_ADMIN`, or `ROLE_TEACHER`

### 4.1 Courses (Teacher)

| Method | Endpoint                                                       | Description              | Status |
|--------|----------------------------------------------------------------|--------------------------|--------|
| POST   | `/api/v1/teacher/courses`                                      | Create course            | 200    |
| GET    | `/api/v1/teacher/courses/my`                                   | My created courses       | 200    |
| GET    | `/api/v1/teacher/courses/{id}`                                 | Get course with modules  | 200    |
| PUT    | `/api/v1/teacher/courses/{id}`                                 | Update course            | 200    |
| DELETE | `/api/v1/teacher/courses/{id}`                                 | Delete course            | 204    |
| POST   | `/api/v1/teacher/courses/{courseId}/modules`                    | Add module               | 200    |
| POST   | `/api/v1/teacher/courses/{courseId}/modules/{moduleId}/lessons` | Add lesson               | 200    |

**Create Course:**
```json
{
  "title": "Introduction to Programming",
  "description": "Learn Python basics",
  "enrollmentPassword": "optional-password",
  "isPublic": true
}
```

**Create Module:**
```json
{ "title": "Module 1: Variables" }
```

**Create Lesson:**
```json
{
  "title": "Variables in Python",
  "content": "A variable is a named storage...",
  "contentType": "TEXT",
  "fileUrl": null
}
```

---

### 4.2 Tests & Quizzes (Teacher)

| Method | Endpoint                                      | Description           | Status |
|--------|-----------------------------------------------|-----------------------|--------|
| POST   | `/api/v1/teacher/tests`                       | Create test           | 200    |
| POST   | `/api/v1/teacher/tests/{testId}/questions`    | Add question          | 200    |

**Create Test:**
```json
{
  "moduleId": 1,
  "title": "Quiz 1: Variables",
  "timeLimitMinutes": 30,
  "randomizeQuestions": true,
  "randomizeChoices": true,
  "passingScore": 70.0
}
```

**Add Question:**
```json
{
  "text": "What is a variable?",
  "questionType": "SINGLE_CHOICE",
  "choices": [
    { "text": "A named storage location", "correct": true },
    { "text": "A function", "correct": false }
  ]
}
```

---

### 4.3 Grades (Teacher)

| Method | Endpoint                                       | Description                | Status |
|--------|------------------------------------------------|----------------------------|--------|
| GET    | `/api/v1/teacher/grades/student/{studentId}`   | Specific student's grades  | 200    |
| POST   | `/api/v1/teacher/grades`                       | Create/assign grade        | 200    |
| DELETE | `/api/v1/teacher/grades/{id}`                  | Delete grade               | 204    |

```json
{
  "studentId": 5,
  "takenClassId": 1,
  "value": 92.5,
  "maxValue": 100.0,
  "gradeType": "MIDTERM",
  "description": "Midterm Exam",
  "date": "2026-04-14"
}
```

---

### 4.4 Attendance (Teacher)

| Method | Endpoint                                              | Description               | Status |
|--------|-------------------------------------------------------|---------------------------|--------|
| POST   | `/api/v1/teacher/attendance`                          | Mark attendance (batch)   | 200    |
| GET    | `/api/v1/teacher/attendance/schedule/{scheduleId}`    | Get class attendance      | 200    |

**Mark Attendance:**
```json
{
  "scheduleId": 1,
  "date": "2026-04-14",
  "attendanceRecords": [
    { "studentId": 1, "status": "PRESENT" },
    { "studentId": 2, "status": "ABSENT" }
  ]
}
```

**Query params for schedule attendance:** `?date=2026-04-14`

---

### 4.5 Enrollments (Teacher)

| Method | Endpoint                                          | Description              | Status |
|--------|---------------------------------------------------|--------------------------|--------|
| GET    | `/api/v1/teacher/enrollments/course/{courseId}`    | Course enrollments       | 200    |

---

### 4.6 Progress (Teacher)

| Method | Endpoint                                               | Description                | Status |
|--------|--------------------------------------------------------|----------------------------|--------|
| GET    | `/api/v1/teacher/progress/courses/{courseId}/students`  | All students' progress     | 200    |

---

### 4.7 Teacher Subjects

| Method | Endpoint                                           | Description              | Status |
|--------|-----------------------------------------------------|--------------------------|--------|
| PUT    | `/api/v1/teacher/teachers/{teacherId}/subjects`    | Update teacher's subjects | 200   |

**Body:** `[1, 2, 3]` (array of subject IDs)

---

## 5. University-Scoped Endpoints

> **Requires:** `ROLE_ADMIN`, `ROLE_UNIVERSITY_ADMIN`, or `ROLE_TEACHER`

| Method | Endpoint                                          | Description               | Status |
|--------|---------------------------------------------------|---------------------------|--------|
| GET    | `/api/v1/university/students`                     | Students by university    | 200    |
| GET    | `/api/v1/university/{universityId}/teachers`      | Teachers by university    | 200    |
| GET    | `/api/v1/university/{universityId}/subjects`      | Subjects by university    | 200    |

**Query params for `students`:** `?universityId={id}`

---

## 6. Public Read Endpoints

> **Requires:** Any authenticated user (all roles)

### 6.1 Universities

| Method | Endpoint                           | Description        | Status |
|--------|-------------------------------------|--------------------|--------|
| GET    | `/api/v1/universities`             | List all           | 200    |
| GET    | `/api/v1/universities/{id}`        | Get by ID          | 200    |
| POST   | `/api/v1/universities`             | Create             | 201    |
| PUT    | `/api/v1/universities/{id}`        | Update             | 200    |
| DELETE | `/api/v1/universities/{id}`        | Delete             | 204    |

---

### 6.2 Faculties (Read)

| Method | Endpoint                                          | Description              | Status |
|--------|---------------------------------------------------|--------------------------|--------|
| GET    | `/api/v1/faculties`                               | List all faculties       | 200    |
| GET    | `/api/v1/faculties/university?universityId={id}`  | Faculties by university  | 200    |
| GET    | `/api/v1/faculties/{id}`                          | Get by ID                | 200    |

---

### 6.3 Teachers/Students (Read)

| Method | Endpoint                              | Description          | Status |
|--------|----------------------------------------|----------------------|--------|
| GET    | `/api/v1/teachers`                    | List all teachers    | 200    |
| GET    | `/api/v1/students/{id}`              | Get student by ID    | 200    |
| GET    | `/api/v1/students/user/{userId}`     | Get by user ID       | 200    |
| GET    | `/api/v1/students/group/{groupId}`   | Students by group    | 200    |
| GET    | `/api/v1/student-groups`             | List all groups      | 200    |
| GET    | `/api/v1/student-groups/{id}`        | Get group by ID      | 200    |

---

### 6.4 Courses (Read)

| Method | Endpoint                     | Description           | Status |
|--------|-------------------------------|-----------------------|--------|
| GET    | `/api/v1/courses/catalog`    | Browse course catalog | 200    |

---

## 7. Student/General Endpoints

> **Requires:** Any authenticated user

### 7.1 Enrollments

| Method | Endpoint                     | Description           | Status |
|--------|-------------------------------|-----------------------|--------|
| POST   | `/api/v1/enrollments`        | Enroll in a course    | 200    |
| GET    | `/api/v1/enrollments/my`     | My enrollments        | 200    |

```json
{
  "courseId": 1,
  "password": "enrollment-password-if-required"
}
```

---

### 7.2 Grades (Read)

| Method | Endpoint                              | Description                        | Status |
|--------|---------------------------------------|------------------------------------|--------|
| GET    | `/api/v1/grades`                      | My grades (all for admins)         | 200    |
| GET    | `/api/v1/grades/subject/{subjectId}`  | My grades by subject               | 200    |
| GET    | `/api/v1/grades/averages`             | My grade averages per subject      | 200    |

---

### 7.3 Schedule (Read)

| Method | Endpoint                                 | Description                      | Status |
|--------|------------------------------------------|----------------------------------|--------|
| GET    | `/api/v1/schedule/week`                  | My weekly schedule (role-based)  | 200    |
| GET    | `/api/v1/schedule/class/{classGroupId}`  | Schedule for a class             | 200    |
| GET    | `/api/v1/schedule/teacher/{teacherId}`   | Schedule for a teacher           | 200    |

> **`/schedule/week` behavior:** Students → their class schedule. Teachers → their schedule. Admins → all.

---

### 7.4 Attendance (Read)

| Method | Endpoint                                                        | Description              | Status |
|--------|------------------------------------------------------------------|--------------------------|--------|
| GET    | `/api/v1/attendance`                                            | My attendance            | 200    |
| GET    | `/api/v1/attendance/stats`                                      | Attendance statistics    | 200    |
| GET    | `/api/v1/attendance/range?startDate={}&endDate={}`              | By date range            | 200    |
| GET    | `/api/v1/attendance/student/{studentId}`                        | Student attendance       | 200    |
| GET    | `/api/v1/attendance/reports/attendance?startDate={}&endDate={}` | Attendance report        | 200    |

> **Date format:** `YYYY-MM-DD` (ISO 8601)

---

### 7.5 Announcements (Read)

| Method | Endpoint                          | Description                  | Status |
|--------|-----------------------------------|------------------------------|--------|
| GET    | `/api/v1/announcements`          | My announcements (role-based)| 200    |
| GET    | `/api/v1/announcements/all`      | All announcements            | 200    |

---

### 7.6 Messages

| Method | Endpoint                                       | Description               | Status |
|--------|------------------------------------------------|---------------------------|--------|
| POST   | `/api/v1/messages`                             | Send message              | 201    |
| GET    | `/api/v1/messages`                             | All my messages           | 200    |
| GET    | `/api/v1/messages/conversations`               | Conversation summaries    | 200    |
| GET    | `/api/v1/messages/conversations/{otherUserId}` | Conversation with user    | 200    |

```json
{
  "recipientId": 5,
  "content": "Hello! When is the assignment due?"
}
```

> **WebSocket:** `ws://136.116.64.6:8080/ws-chat`

---

### 7.7 Tests (Student)

| Method | Endpoint                                       | Description                | Status |
|--------|-------------------------------------------------|----------------------------|--------|
| GET    | `/api/v1/tests/{testId}`                       | Get test details           | 200    |
| GET    | `/api/v1/tests/{testId}/questions`             | Get questions              | 200    |
| POST   | `/api/v1/tests/{testId}/attempts`              | Start an attempt           | 200    |
| POST   | `/api/v1/tests/attempts/{attemptId}/submit`    | Submit answers             | 200    |

**Submit Answers:**
```json
{
  "answers": [
    { "questionId": 1, "choiceIds": [3] },
    { "questionId": 2, "choiceIds": [5, 7] }
  ]
}
```

---

### 7.8 Progress

| Method | Endpoint                                        | Description             | Status |
|--------|-------------------------------------------------|-------------------------|--------|
| POST   | `/api/v1/progress/lessons/{lessonId}/complete`  | Mark lesson complete    | 200    |
| GET    | `/api/v1/progress/courses/{courseId}`            | My course progress      | 200    |

---

## 8. Enums Reference

### Roles

| Value                  | Description              |
|------------------------|--------------------------|
| `ROLE_ADMIN`           | Platform super admin     |
| `ROLE_UNIVERSITY_ADMIN`| University administrator |
| `ROLE_TEACHER`         | Teacher / Instructor     |
| `ROLE_STUDENT`         | Student                  |
| `ROLE_ACCOUNTANT`      | Accountant               |
| `ROLE_GUEST`           | Guest / Limited access   |

### Content Type

| Value   | Description     |
|---------|-----------------|
| `TEXT`  | Rich text       |
| `PDF`   | PDF document    |
| `VIDEO` | Video content  |

### Attendance Status

| Value     | Description          |
|-----------|----------------------|
| `PRESENT` | Student was present  |
| `ABSENT`  | Student was absent   |
| `LATE`    | Student was late     |
| `EXCUSED` | Excused absence      |

### Question Type

| Value             | Description              |
|-------------------|--------------------------|
| `SINGLE_CHOICE`   | One correct answer      |
| `MULTIPLE_CHOICE`  | Multiple correct answers|

### Day of Week

`MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

---

## 9. Role-Based Access Matrix

| URL Prefix           | ADMIN | UNI_ADMIN | TEACHER | STUDENT |
|----------------------|:-----:|:---------:|:-------:|:-------:|
| `/auth/**`           | 🌐    | 🌐        | 🌐      | 🌐      |
| `/admin/**`          | ✅    | ✅        | ❌      | ❌      |
| `/teacher/**`        | ✅    | ✅        | ✅      | ❌      |
| `/university/**`     | ✅    | ✅        | ✅      | ❌      |
| Everything else      | ✅    | ✅        | ✅      | ✅      |

🌐 = Public (no auth required)

---

## 10. Error Handling

| Code | Meaning                                  |
|------|------------------------------------------|
| 200  | Success                                  |
| 201  | Created                                  |
| 204  | No Content (successful delete)           |
| 400  | Bad Request (validation error)           |
| 403  | Forbidden (no token or insufficient role)|
| 404  | Not Found                                |
| 500  | Internal Server Error                    |

### Frontend Error Handler

```javascript
async function apiCall(url, options) {
  try {
    const response = await fetchWithAuth(url, options);
    if (response.status === 204) return null;
    if (!response.ok) {
      if (response.status === 403) throw new Error('Access denied');
      throw new Error(`HTTP ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error(`API Error [${url}]:`, error.message);
    throw error;
  }
}
```

---

## Authentication Flow

```
┌─────────┐                          ┌──────────┐
│ Frontend │  POST /auth/register    │  Backend │
│          │ ──────────────────────→ │          │
│          │ ←── { success: true }   │          │
│          │                         │          │
│          │  POST /auth/login       │          │
│          │ ──────────────────────→ │          │
│          │ ←── { access_token,     │          │
│          │       refresh_token }   │          │
│          │                         │          │
│          │  GET /api/v1/grades     │          │
│          │  Authorization: Bearer  │          │
│          │ ──────────────────────→ │          │
│          │ ←── [grade data]        │          │
│          │                         │          │
│          │  ── Token Expired ──    │          │
│          │ ←── 403 Forbidden       │          │
│          │                         │          │
│          │  POST /auth/refresh     │          │
│          │  { refresh_token }      │          │
│          │ ──────────────────────→ │          │
│          │ ←── { new tokens }      │          │
└─────────┘                         └──────────┘
```

---

## 11. Test Credentials (Data Seeder)

The backend database is automatically seeded with the following test accounts on startup. You can use these immediately for frontend testing.

| Role | Email | Password | Details |
|------|-------|----------|---------|
| **Platform Admin** | `superadmin@edupage.com` | `super123` | Super Admin |
| **University Admin** | `admin@edupage.com` | `admin123` | Admin for Alatoo International University |
| **Teacher** | `teacher@edupage.com` | `teacher123` | John Smith (Math, Physics) |
| **Teacher** | `teacher1@edupage.com` | `teacher123` | Jane Doe (Computer Science) |
| **Student** | `student1@edupage.com` | `student123` | Alice Johnson (Group 10A) |

> **Note:** All users are fully enabled and ready for authentication via the `/api/v1/auth/login` endpoint.

### Sample Course Content
For testing course progress and enrollments, the following content is also automatically seeded:
* **Course:** `Introduction to Computer Science` (Teacher: Jane Doe)
  * **Module:** `Module 1: Getting Started`
    * **Lesson:** `Variables in Programming` (Text content)
    * **Quiz/Test:** `Quiz 1: Basics of Programming` (15 min limit)
      * Includes sample `SINGLE_CHOICE` questions (e.g. "What is a variable?").
