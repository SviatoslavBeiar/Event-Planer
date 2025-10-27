# 🎟️ Evently — Event Management & Ticket Verification Platform

**Evently** is a modern platform for creating, managing, and verifying events.  
Organizers can create events, sell or distribute tickets, and assign checkers to verify attendees.  
Users can register for events, receive tickets, and participate in activities.

---

## ⚙️ Technologies

### 🖥️ Backend — **Spring Boot (Java 17)**
- **Spring Security + JWT** — authentication and authorization  
- **Spring Data JPA / Hibernate** — database interaction  
- **MapStruct** — automatic mapping of entities to DTOs  
- **Lombok** — reduces boilerplate (getters, setters, constructors)  
- **Validation API** — request validation  
- **Swagger / OpenAPI** — REST API documentation  
- **MySQL / PostgreSQL** — database (configured via JPA)

---

### 💻 Frontend — **React + Chakra UI**
- **Axios** — HTTP requests to the backend  
- **Formik + Yup** — form handling and validation  
- **React Router v6** — routing  
- **Context API** — user state management (AuthContext)  
- **Chakra UI** — modern responsive UI  
- **Toast notifications** — user feedback

---

## 🔐 User Roles
- **USER** — can view and register for events  
- **ORGANIZER** — creates events, manages tickets, and assigns checkers

---

## 🧩 Key Features
- 📅 Create, edit, and publish events  
- 🎫 User registration and unique ticket generation  
- ✅ Ticket verification by organizers and checkers  
- 👥 Assign or revoke checkers by ID or email  
- 🧾 Track event capacity and attendance

---

## 🚀 Project Setup
```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
