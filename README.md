# Pickleball Court Booking Platform
Fresher Onboarding Assignment

# 🏓 PicklePlay – Sports Venue Booking Platform

PicklePlay is a full-stack web application where users can discover and book sports venues, and venue owners can manage their courts, schedules, and bookings.

---

## Features

### User (Booker)

* Browse available sports venues
* View real-time slot availability
* Book courts for selected time slots
* Add bookings to cart
* Reschedule bookings
* Cancel bookings
* View booking history (Upcoming / Past / Cancelled)

---

### Owner

* Create and manage venues
* Upload and manage venue photos (max 5)
* Edit venue details (pricing, timing, courts, etc.)
* Delete venue
* View owned venues in dashboard
* Manage venue bookings

---

## Core Functionalities

* 🔐 JWT-based Authentication
* 🎭 Role-based access (BOOKER / OWNER)
* 📅 Real-time slot availability system
* 🛒 Cart system with expiration logic
* 🔁 Booking reschedule feature
* 🖼️ Image upload (stored as BLOB in DB)
* 📊 Owner dashboard

---

## Tech Stack

### Frontend

* React.js
* Tailwind CSS
* Axios
* React Router

### Backend

* Spring Boot
* Spring Security (JWT)
* Hibernate / JPA

### Database

* MySQL

---

## 📂 Project Structure

```
frontend/
  ├── components/
  ├── pages/
  ├── services/api.js
  └── context/AuthContext.js

backend/
  ├── controller/
  ├── service/
  ├── repository/
  ├── entity/
  └── security/
```

---

## Setup Instructions

### Backend Setup

1. Navigate to backend folder
2. Configure MySQL in `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/court_booking
spring.datasource.username=your_username
spring.datasource.password=your_password

app.base-url=http://localhost:8080
```

3. Run the application:

```terminal
mvn spring-boot:run
```

---

### Frontend Setup

1. Navigate to frontend folder

```terminal
npm install
npm run dev
```

2. Open browser:

```
http://localhost:5173
```

---

## API Highlights

### Auth

* `POST /auth/login`
* `POST /auth/register`

### Venue

* `POST /venues` (OWNER)
* `GET /venues`
* `GET /venues/{id}`
* `PUT /venues/{id}` (OWNER)
* `DELETE /venues/{id}` (OWNER)
* `GET /venues/owner`

### Photos

* `POST /venues/{venueId}/upload`
* `GET /venues/photo/{id}`
* `DELETE /venues/{venueId}/photos/{photoId}`

### Booking

* `POST /booking/add`
* `PUT /booking/reschedule/{id}`
* `DELETE /booking/cancel/{id}`
* `GET /booking/history`

### Availability

* `GET /availability/{venueId}?date=yyyy-MM-dd`

---

## Image Handling

* Images are stored as `LONGBLOB` in MySQL
* Max 5 images per venue
* Uploaded one-by-one via API
* Retrieved using `/venues/photo/{id}`

---

## Important Notes

* Only owners can manage their venues
* Slot availability prevents double booking
* Cart bookings expire after 10 minutes
* Past time slots are automatically disabled

---

## 🧪 Sample Roles

| Role   | Access        |
| ------ | ------------- |
| BOOKER | Book courts   |
| OWNER  | Manage venues |

---

## Future Enhancements

* Thumbnail selection for venue
* Owner analytics dashboard
* Advanced search & filters
* Map integration (Google Maps)
* Reviews & ratings
* Mobile responsive UI improvements

---

## Author

**Sweekar Chougale**
Full Stack Developer (Java + React)

---

## Project Description

> Built a full-stack sports venue booking platform using Spring Boot and React, enabling real-time court booking, role-based access, and venue management with image upload and scheduling features.

---

## If you like this project

Give it a ⭐ on GitHub!

---

