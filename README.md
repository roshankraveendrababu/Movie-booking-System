
# Multi Threaded movie booking system

This project is a comprehensive, multi-threaded movie ticket booking system designed to run as an interactive command-line application. It simulates a real-world booking environment where multiple users can browse movies and book seats concurrently. To ensure data integrity under high contention, the system employs an advanced **ReentrantReadWriteLock**  mechanism, allowing for simultaneous read operations while guaranteeing exclusive access for write operations like booking.

A key feature is the autonomous background service, built with ScheduledExecutorService, that automatically handles abandoned bookings by releasing expired seat locks. The system's architecture is built on sound object-oriented principles, showcasing the *Factory pattern* for creating different payment strategies and the *Observer pattern* to decouple post-booking actions, such as sending notifications or updating analytics. This project serves as a practical demonstration of robust backend engineering concepts, including advanced concurrency, system design, and common design patterns.


## Features

- **Advanced Concurrency Control**: Employs a ReentrantReadWriteLock to manage simultaneous booking requests. This allows for high-performance, non-blocking reads (multiple users viewing seats) while ensuring thread-safe, exclusive writes (a single user completing a booking).

- **Autonomous Lock Cleanup**: Utilizes a *ScheduledExecutorService* to run a background task that automatically detects and releases seat locks from abandoned user sessions after a timeout, ensuring seats are returned to the available pool.
- **SOLID Design Patterns**: Implements key patterns for a decoupled and extensible architecture:

    ***Factory Pattern*** to dynamically create different *PaymentStrategy* objects (e.g., UPI, Debit Card).

    ***Observer Pattern*** to notify other components (like an email or analytics service) of successful bookings without coupling them to the BookingService.

    ***Strategy Pattern*** to allow various payment methods to be used interchangeably.


- **Layered Architecture**: Built with a clear separation of concerns, dividing the application into distinct layers (Controllers, Services, Core Entities). This improves maintainability, testability, and makes the codebase easier to navigate and debug.


## Tech Stack

**Core Language:** Java (17+)

**Concurrency:**

Java Concurrency Utilities (java.util.concurrent)

ScheduledExecutorService

ReentrantReadWriteLock

ConcurrentHashMap

**Design & Architecture**:

Layered Architecture (Controller-Service)

Factory Pattern

Observer Pattern

Strategy Pattern


## Screenshots

![Choice options ](https://github.com/user-attachments/assets/63a852e6-e3ec-4712-b36d-f6c1cc1ef5f3)


![Concurrency with threads](https://github.com/user-attachments/assets/8974f716-d940-4223-9e0c-0b2d3e74c87b)


![Background thread cleaning expired bookings](https://github.com/user-attachments/assets/8e336a89-c56f-430f-9fdd-89eb6a37d8b3)


![Successful seat booking](https://github.com/user-attachments/assets/71e6d562-b671-4484-a90a-9335e0a806cc)

## Demo Video


https://github.com/user-attachments/assets/9dd28327-a637-47e8-a3c3-4e6567689530
## Authors

- [@Roshan.K.R](https://github.com/roshankraveendrababu)


## 🔗 Links
[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/roshan-konidhala/)
[![twitter](https://img.shields.io/badge/twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://x.com/RoshanKR0912) 

