# 10x Flashcards

A smart flashcard application leveraging AI to automatically generate educational flashcards from text input.

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Project Status](#project-status)
- [License](#license)

## Project Description

10x Flashcards is an application designed to streamline the learning process by automatically generating educational
flashcards using AI. The application aims to solve the primary challenge of manual flashcard creation, which is often
time-consuming and labor-intensive.

**Key Features:**

- AI-powered flashcard generation from text input (up to 10,000 characters)
- Review, edit, and delete generated flashcards in a grid view
- Spaced repetition system with randomly selected flashcards
- User authentication and authorization for personalized content
- Row-Level Security (RLS) implemented for data isolation between users

## Tech Stack

### Backend

- **Java 21**
- **Spring Boot 3.4.4**
- **Spring Security** - for user authentication and authorization
- **Spring Data JPA** - for database operations
- **Lombok** - to reduce boilerplate code
- **MapStruct** - for object mapping between layers
- **Springdoc OpenAPI** - for API documentation (Swagger)
- **Flyway** - for database migrations

### Frontend

- **Angular 19.2**
- **Angular Material** - UI components
- **RxJS** - for reactive data streams
- **NgRx** - state management (optional)
- **Angular JWT** - JWT token handling

### Database

- **PostgreSQL 17.4** in Docker container
- **Row-Level Security** for data isolation

### AI/LLM

- **Ollama** in Docker container
- **gemma2:2b** model
- **LangChain4j** - for LLM integration in Java application

### DevOps/Infrastructure

- **Docker** - containerization of all components
- **Docker Compose** - container orchestration in local environment
- **GitHub Actions** - CI/CD
- **VPS cal.pl** - hosting

### Testing

- **JUnit 5** - unit tests
- **Mockito** - component mocking
- **Testcontainers** - integration tests with containers
- **Karma/Jasmine** - Angular tests

## Getting Started Locally

### Prerequisites

- Docker and Docker Compose
- pnpm 10.8.0 or higher (for frontend development)
- Java 21 (for backend development outside Docker)

### Setting Up the Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/JacobTheLiar/10x-flashcards.git
   cd 10x-flashcards
   ```

2. Create `.env` file in the root directory with the following variables:
   ```
   SUPER_USER=postgres
   SUPER_PASSWORD=yourpassword
   DB_NAME=flashcards
   DB_USER=flashcards_user
   DB_PASSWORD=flashcards_password
   BACKEND_PORT=8080
   FRONTEND_PORT=4200
   SPRING_PROFILES_ACTIVE=dev
   ```

3. Start the application using Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. Access the application:
    - Frontend: http://localhost:4200
    - Backend API: http://localhost:8080
    - API Documentation: http://localhost:8080/swagger-ui.html

### Development Setup

#### Frontend Development

```bash
cd source/frontend
pnpm install
pnpm start
```

#### Backend Development

```bash
cd source/backend
./mvnw spring-boot:run
```

## Available Scripts

### Frontend Scripts

- `pnpm start` - Start development server with proxy configuration
- `pnpm build` - Build the frontend application
- `pnpm watch` - Build and watch for changes in development mode
- `pnpm test` - Run frontend tests
- `pnpm update-api` - Update API client from OpenAPI specification

### Backend Scripts

- `./mvnw clean install` - Clean and install the backend application
- `./mvnw spring-boot:run` - Run the backend application
- `./mvnw test` - Run backend tests

### Docker Commands

- `docker-compose up -d` - Start all services in detached mode
- `docker-compose down` - Stop all services
- `docker-compose logs -f` - Follow logs from all services

## Project Scope

### MVP Features

- AI-based flashcard generation from pasted text (up to 10,000 characters)
- Flashcard management (review, edit, delete) in a grid layout
- Simple spaced repetition system with randomly selected flashcards (5-15 cards)
- User authentication and authorization
- Data isolation between users via Row-Level Security

### Current Limitations

- No manual flashcard creation functionality
- Simple random algorithm instead of advanced spaced repetition (like SuperMemo or Anki)
- Text input only (no support for PDF, DOCX imports)
- No flashcard set sharing between users
- Desktop browser optimization only

## Project Status

The project is currently in development phase. MVP features are being implemented according to the Product Requirements
Document.

## License

[MIT](https://choosealicense.com/licenses/mit/)

---

*Note: This project is developed as part of the 10x_Devs initiative.*