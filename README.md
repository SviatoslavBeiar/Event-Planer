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
- 🧾  automatically generate QR codes and downloadable PDF tickets  
- 📧  send tickets directly to users via email
- ✅ **Email Validation** — ensure valid user email addresses during registration  
---

## 🌟 Future Enhancements 
- 🔐 **OAuth 2.0 Authentication** — enable login via Google, Facebook, or other providers  
- 🤖 **Machine Learning Integration** — analyze user behavior to identify interests and suggest personalized events  
- 📍 **Nearby Events** — show events based on user’s current location or city
  
  ---
## Graphical Diagram (ERD)
The diagram below illustrates the database structure and the relationships between entities.

<img width="1004" height="768" alt="image" src="https://github.com/user-attachments/assets/47b6031c-d642-45a6-8a31-b1ba212620b0" />
