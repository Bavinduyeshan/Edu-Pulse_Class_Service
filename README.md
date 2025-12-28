# 📚 EduPulse - Class Management & Attendance Service
> The academic core for course organization, lecture scheduling, and real-time attendance tracking in the EduPulse learning platform.

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/)
[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![OpenFeign](https://img.shields.io/badge/OpenFeign-00ADD8?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud-openfeign)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue?style=for-the-badge)](#)

---

## 📖 Project Overview

The **Class Service** is the academic heart of EduPulse, managing the complete lifecycle of courses, lectures, and attendance tracking. Lecturers can create classes, schedule lectures, and monitor student participation, while students can view their class schedules and attendance records.

Built with **Spring Cloud OpenFeign** for seamless inter-service communication, this service validates lecturer credentials, manages grade-based class organization, and provides comprehensive attendance analytics.

### 🏗 Microservices Intercommunication

This service orchestrates multiple aspects of the learning experience:

* **👤 Lecturer Validation:** Verifies lecturer credentials via `User-Service` before class creation.
* **🎓 Grade Association:** Links classes to grade levels maintained by `User-Service`.
* **📝 Enrollment Integration:** Provides class data to `Enrollment-Service` for student registration.
* **🎯 Quiz Coordination:** Supplies lecture context to `Quiz-Service` for assessment scheduling.
* **📊 Admin Analytics:** Feeds lecture statistics to `Admin-Service` for platform insights.

---

## 🚀 Key Features

* **📚 Class Management:** Create, update, and organize courses by grade level and subject.
* **📅 Lecture Scheduling:** Time-based lecture planning with detailed descriptions and materials.
* **✅ Attendance Tracking:** Real-time attendance marking with status (Present/Absent/Late).
* **👨‍🏫 Lecturer Dashboard:** View all classes, lectures, and attendance analytics per lecturer.
* **🎓 Grade-Based Organization:** Filter and retrieve classes by academic grade level.
* **📊 Attendance Reports:** Comprehensive attendance history for individual students and lectures.
* **🔒 Role-Based Access:** Secure endpoints with lecturer, student, and admin permissions.
* **🔄 CRUD Operations:** Full lifecycle management for classes and lectures.

---

## 🛠 Tech Stack

* **Backend:** Java 21, Spring Boot 3.5.0
* **Security:** Spring Security 6.x, JWT Authentication
* **Database:** MySQL with JPA/Hibernate
* **Build Tool:** Maven
* **Inter-Service Comm:** OpenFeign Client
* **Validation:** Hibernate Validator
* **DevOps:** Spring DevTools

---

## 📡 API Documentation (V1)

### 📚 Class Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/classes` | Create a new class | Lecturer |
| `GET` | `/api/classes/classes/{classId}` | Get class details by ID | Authenticated |
| `GET` | `/api/classes` | Get all classes | Lecturer/Admin |
| `GET` | `/api/classes/grade/{gradeId}` | Get classes by grade level | Authenticated |
| `GET` | `/api/classes/lecturer/{lecturerId}` | Get all classes for a lecturer | Lecturer/Admin |
| `PUT` | `/api/classes/{classId}` | Update class details | Lecturer |
| `DELETE` | `/api/classes/{classId}` | Delete/archive a class | Lecturer/Admin |

### 📅 Lecture Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/classes/{classId}/lectures` | Schedule a new lecture | Lecturer |
| `GET` | `/api/classes/{classId}/lectures` | Get all lectures for a class | Authenticated |
| `GET` | `/api/classes/lectures/{lectureId}` | Get lecture details by ID | Authenticated |
| `PUT` | `/api/classes/lectures/{lectureId}` | Update lecture information | Lecturer |
| `DELETE` | `/api/classes/lectures/{lectureId}` | Delete a lecture | Lecturer/Admin |

### ✅ Attendance Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/classes/lectures/{lectureId}/attendance` | Mark student attendance | Lecturer/Student |
| `GET` | `/api/classes/lectures/{lectureId}/attendance` | Get attendance for a lecture | Lecturer |
| `GET` | `/api/classes/students/{studentId}/attendance` | Get attendance history for student | Student/Lecturer/Admin |
| `GET` | `/api/classes/lectures/{lectureId}/my-attendance` | Get my attendance for a lecture | Student |

### 📊 Analytics & Admin

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/classes/lectures/count` | Get total lectures count | Admin |

---




## 🔗 Related Services

- [🌐 API Gateway](https://github.com/Bavinduyeshan/Edu-Pulse-Gateway)
- [📚 Class Service](https://github.com/Bavinduyeshan/Edu-Pulse_Class_Service)
- [📝 Enrollment Service](https://github.com/Bavinduyeshan/Edu-Pulse-Entrollment-Service)
- [🎯 Quiz Service](https://github.com/Bavinduyeshan/Edu-Pulse-Quiz_Service)
- [👨‍💼 Admin Service](https://github.com/Bavinduyeshan/Edu-Pulse_Admin_Service)

---

<div align="center">

**Built with ❤️ for better education management**

⭐ Star this repository if you find it helpful!

</div>
