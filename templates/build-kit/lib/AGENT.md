# Agent Learnings

Patterns and gotchas discovered during task processing. Update this file whenever you encounter something reusable.

## Cratis non-negotiables (seed — full detail in `.claude/skills/_shared/cratis-conventions.md`)

- ALL backend artifacts for a slice go in the slice's ONE folder — Java requires one public type per file, so a slice is `<Module>/<Feature>/<Slice>/` with one artifact per file (the shipped starter uses `<Module>/<Feature>/<Slice>/`). Never split into `commands/`, `handlers/`, `events/`.
- `@Command` records define `handle()` directly on the record — never a separate handler class. The `@CommandKey` property is the event source id the returned event is appended to.
- `@EventType` takes NO annotation arguments (the type name is the id); events are past-tense records, no nullable properties.
- Read models: `@ReadModel` (Arc) + `@ReadModel` (Chronicle) records expose queries as static methods with `@Path` returning `CompletionStage`; the query body reads through a reader interface. Reducers build read models from events; projections join events, never read models.
- Reactors are `@Reactor` classes; dispatch is by the parameter type. Reducers are `@Reducer` classes returning the new read-model state. Keep reactors idempotent and stateless.
- Package mirrors the folders: `io.cratis.<Module>.<Feature>.<Slice>` (the starter's root is `io.cratis`). Find the root from `build.gradle.kts` / existing slices; never hard-code.
- The frontend of a slice cannot reference its proxy until the backend compiles. Order: Backend → `./gradlew build` → Specs → Frontend → Composition.
- Quality gate: `./gradlew build` with zero warnings/errors; `./gradlew test --tests "*<SliceName>*"`.

## tasks.json

- Tasks are objects with `id`, `createdAt`, and `payload` (a `SliceChangedPayload`).
- After completing a task, remove it from the array entirely — do not add a status field.
- Write `[]` to `tasks.json` if the last task is completed.

## SliceChangedPayload fields

```
event           always "slice:changed"
organizationId  org UUID or null
boardId         board UUID
sliceId         SLICE_BORDER node UUID — use this with /load-slice
sliceTitle      human-readable slice name (may be null)
sliceStatus     e.g. "Created", "InProgress", "Done", "Blocked" (may be null)
timestamp       unix ms when the change was emitted
```

## Slice files

The realtime agent writes one file per slice on startup and after each `slice:changed` event:

```
.slices/<context>/<sliceName>/slice.json
```

- `<context>` is the slice's context value, or `default` if none.
- `<sliceName>` is the slice title lowercased with spaces removed (e.g. `"Enable User"` → `enableuser`).

These files are always up to date — read them directly before invoking any skill.

## Skill Usage

- Always run `/connect` first to load credentials from `.eventmodelers/config.json` before calling any other skill.
- `/load-slice sliceId=<uuid>` re-fetches all slices from the API, refreshes the slice files, and returns the requested slice. Use it when you need a guaranteed-fresh view of a specific slice.
- Read `.slices/<context>/<sliceName>/slice.json` directly when you already know the context and name and the file is recent enough.

## Board API

- The `boardId` and `organizationId` from each payload provide full context — pass them to skills.
- Node events use `node:created`, `node:changed`, `node:deleted` — always POST to `/api/org/:orgId/boards/:boardId/nodes/events`.
- Slice metadata (title, status) lives on the SLICE_BORDER node under `meta.sliceStatus` and `meta.title`.
