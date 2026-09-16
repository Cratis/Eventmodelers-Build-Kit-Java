# Java App

A Java application built with Cratis Chronicle.

## Prerequisites

- JDK 17 or later
- Gradle 8.0 or later

## Getting Started

```bash
./gradlew build
./gradlew run
```

## Project Structure

- `src/main/java/io/cratis/JavaApp.java` — Application entry point
- `SomeModule/SomeFeature/SomeSlice.java` — Example slice demonstrating the Cratis way

## Build Slices

Use the kit's skills to implement slices:

- `/build-state-change` — for commands and events
- `/build-state-view` — for read models and projections
- `/build-automation` — for reactors and pipelines

Follow the conventions in `.cratis-build-kit/.claude/skills/_shared/cratis-conventions.md`.
