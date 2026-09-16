# CratisApp

A Java application built with Cratis Arc and Chronicle, with the shared Cratis React frontend.

## Prerequisites

- JDK 17 or later
- Node.js 18+ (frontend)

## Getting Started

```bash
docker-compose up -d   # Chronicle development kernel
./gradlew build        # backend
./gradlew bootRun      # run the backend (http://localhost:8080)
npm install && npm run dev   # frontend dev server
```

## Project Structure

- `src/main/java/io/cratis/Application.java` — application entry point
- `SomeModule/SomeFeature/` — example slice demonstrating the Cratis way
- `Tests/SomeModule/SomeFeature/` — example slice tests
- `.frontend/` — Vite scaffolding for the React frontend

## Build Slices

Use the kit's skills to implement slices:

- `/build-state-change` — for commands and events
- `/build-state-view` — for read models and projections
- `/build-automation` — for reactors

Follow the conventions in `.build-kit/.claude/skills/_shared/cratis-conventions.md`.
