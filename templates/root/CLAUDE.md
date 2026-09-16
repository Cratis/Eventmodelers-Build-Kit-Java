# CratisApp — agent conventions

This is a **Cratis** application: Cratis Arc (CQRS) + Cratis Chronicle (event sourcing) in **Java**
(JVM, Spring Boot), with the shared Cratis React + TypeScript frontend (Vite + PrimeReact).

Build slices with the kit's skills — `/build-state-change`, `/build-state-view`, `/build-automation` —
and follow the conventions in `.build-kit/.claude/skills/_shared/cratis-conventions.md`.
**The shipped example slice under `SomeModule/SomeFeature/` is the concrete pattern to copy** —
match its structure exactly.

## Structure (learn from `SomeModule/SomeFeature/`)

```
<Module>/<Feature>/
├── <Feature>.tsx            ← composition page (buttons, dialogs, tables)
├── index.ts                 ← barrel: export * from './<Feature>'
└── <Slice>/                 ← one slice = one behavior
    ├── <Command>.java       ← command record with handle()
    ├── <Event>.java         ← event record
    ├── <Reactor>.java       ← reactor (optional)
    ├── <ReadModel>.java     ← read model record (state views)
    ├── <Reader>.java        ← reader exposing the read model as a query
    ├── <Proxy>.ts           ← TypeScript proxies for the frontend
    ├── <Component>.tsx      ← React component using the proxy
    └── index.ts             ← barrel export
```

- Top folder is a **module**, then a **feature**, then **slices**. Package is
  `io.cratis.<Module>.<Feature>.<Slice>` (the root package is `io.cratis` — see `build.gradle.kts`).
- Java requires one public type per file — a slice is **one folder**, one artifact per file.
- The `<Module>/` folders are wired into the Gradle `main` source set, and `Tests/` mirrors them as
  the `test` source set.

## Non-negotiables (full detail in the shared conventions doc)

- ALL backend artifacts for a slice live in the slice's ONE folder — never split into
  `commands/`, `handlers/`, `events/` trees.
- `@Command` records define `handle()` directly — never separate handler classes. The `@CommandKey`
  property is the event source id the returned event is appended to.
- `@EventType` takes NO arguments; events are past-tense records, never nullable properties.
- `@ReadModel` (Arc) + `@ReadModel` (Chronicle) records expose queries as static methods with
  `@Path`, returning `CompletionStage`; the query body reads through a reader interface
  (`@FromServices`).
- Reactors are `@Reactor` classes; dispatch is by the parameter type. Reducers are `@Reducer` classes
  returning the new read-model state. Keep reactors idempotent + stateless.
- Frontend: `CommandDialog` / `InputTextField` from `@cratis/components/*`; never import `Dialog`
  from `primereact/dialog`. PrimeReact CSS variables / Tailwind classes for styling; no `any`.

## Build, run, test

```bash
docker-compose up -d        # Chronicle development kernel
./gradlew build             # compiles backend (KSP generates the Arc manifest)
./gradlew test              # run specs (filter while iterating: --tests "*<Slice>*")
npm install && npm run dev  # frontend dev server (Vite)
./gradlew bootRun           # backend (http://localhost:8080)
```

**Sequencing is strict:** a slice's frontend cannot reference its proxy until the backend compiles,
so always go Backend → `./gradlew build` → Specs → Frontend → Composition page → Routes (`App.tsx`).

## Testing

Write tests in the `Tests/` folder that mirror your slice structure:

```
Tests/
└── SomeModule/
    └── SomeFeature/
        └── RegistrationTests.java  ← tests for the Registration slice
```

```bash
./gradlew test                                  # run all tests
./gradlew test --tests "*RegistrationTests*"    # run specific slice tests
```

**Test conventions:**
- Tests live in `Tests/<Module>/<Feature>/<SliceName>Tests.java`
- Use `CommandScenario` with `BlockingCommandScenario` from `io.cratis:arc-testing`
- Tests are marked with `@Test` (JUnit 5)
- Use `ChronicleCommandScenarios.chronicle(...).shouldHaveAppendedEvent(...)` to assert events
- Maintain the same structure as your implementation slices

Full detail: `.build-kit/.claude/skills/_shared/cratis-conventions.md`.

## Learn more

- Cratis docs: https://www.cratis.io/
- Conventions: `.build-kit/.claude/skills/_shared/cratis-conventions.md`
