---
name: build-state-change
description: >
  Implement Event Sourcing write slices the Cratis way — using Cratis Arc (CQRS) + Cratis Chronicle
  (event sourcing) in a Java project. A write slice is: Command → handle() → Event(s), with
  optional validators and business rules. Use when: (1) implementing a new write
  slice / command in a Cratis project, (2) a slice.json has a non-empty commands[] / events[] section,
  (3) the user provides an Event Modeling artifact, specification, or natural-language description of a
  command and asks to implement it, (4) the user says "implement", "create", "add" a write slice,
  command, or state change in a Cratis Arc / Chronicle project.
---

# Cratis — Write Slice (State Change)

A write slice mutates state by recording events. In Cratis Arc the slice lives in **one folder** —
Java requires one public type per file, so each artifact gets its own file inside the slice folder:

```
<Module>/<Feature>/<Slice>/
├── <Command>.java        ← @Command record + handle()
├── <Event>.java          ← @EventType record(s)
└── <Reactor>.java        ← optional reactor
```

> **Read first:** [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the
> non-negotiable Cratis rules. Everything below assumes them.

## Step 0 — Discover the target project's conventions

Before writing code, read the project's `CLAUDE.md` and **at least one existing slice** (the starter
ships one under `SomeModule/SomeFeature/`). Confirm:

- The package root (read `build.gradle.kts` and existing `.java` files — never hard-code it; the
  package mirrors the folders).
- How existing commands return results (single event / `CompletionStage` / `Pair` / no result).
- Whether existing `.java` files use a file header (the starter uses none).

> **Comments & description:** each slice element carries a `comments: string[]` array and a
> `description`. Use them as implementation hints. When done, resolve each used comment:
> `POST <BASE_URL>/api/org/<ORG_ID>/boards/<BOARD_ID>/nodes/<nodeId>/comments/<commentId>/resolve`
> (get IDs first via GET on the same path).

## Step 1 — Understand the input (`slice.json` is the source of truth)

Extract, regardless of input format:

| Element | What to extract |
|---|---|
| **Command** | Name (imperative), fields, which field is the event source id (`@CommandKey`) |
| **Events** | Names (past tense), fields, which events this command appends |
| **Business rules** | Preconditions, invariants, idempotency — from `description` / `comments` only |
| **State needed for rules** | Which read model must be inspected to evaluate a rule |
| **Specifications** | Each GWT / scenario maps 1:1 to an executable spec |

**If a field is not in `slice.json`, it does not go in the code.** If requirements are unclear, ask
the user before proceeding.

### Determine the trigger
If unclear how the command is dispatched, ask:
> - **UI / REST** — exposed automatically by Arc; add a React component + integration spec.
> - **Automation only** — dispatched internally by a reactor (no UI). The command still exists; no `.tsx`.

## Step 2 — Write the slice files

`<Module>/<Feature>/<Slice>/` — ALL backend artifacts in this one folder; package
`io.cratis.<Module>.<Feature>.<Slice>` (no file header unless the project uses one).

### Events first
```java
import io.cratis.chronicle.events.EventType;

@EventType                                  // NEVER any arguments
public record AuthorRegistered(String name) {
}
```
Past tense, records, no nullable properties, one purpose each. If the context's events already exist
elsewhere, reuse them — don't redefine.

### Command with `handle()` on the record
```java
import io.cratis.arc.artifacts.Command;
import io.cratis.arc.artifacts.CommandKey;
import io.cratis.arc.authorization.AllowAnonymous;

@Command
@AllowAnonymous
public record RegisterAuthor(@CommandKey String id, String name) {
    /** Produces the event appended to the command-key event source. */
    public AuthorRegistered handle() {
        return new AuthorRegistered(name);
    }
}
```

Pick the return shape that matches the slice:
- **single event** → `public EventName handle()`
- **asynchronous** → `public CompletionStage<EventName> handle()`
- **multiple values / responses** → `Pair` / `ArcOneOf` (Arc flattens the response; see
  `cratis-conventions.md`)
- **side-effect only** → no return value from `handle()`

Event source: the `@CommandKey` property is the event source id.

### Business rules — inject the read model
When a rule depends on event-sourced state, take the read model as an additional `handle()` parameter;
Arc injects current state. Only encode rules that appear in the slice `description` / `comments`. See
[references/patterns.md](references/patterns.md) for the read-model injection and error patterns.

## Step 3 — Build

From the project root: `./gradlew build`. Fix ALL warnings and errors before continuing. KSP generates
the Arc manifest (routes, contracts) as part of the build.

## Step 4 — Write specs (one per scenario in `slice.json`)

Put specs in `Tests/<Module>/<Feature>/<SliceName>Tests.java`. Cover, from the slice's specifications:
- **Happy path** — command succeeds, correct event appended.
- **Each validation failure** — one spec per rule.
- **Each business-rule violation** — one spec per read-model condition.

Use `CommandScenario` with `BlockingCommandScenario` from `io.cratis:arc-testing`:

```java
var configured = new CommandScenario<>(module, RegisterAuthor.class);
try (var scenario = new BlockingCommandScenario<>(configured)) {
    scenario.execute(new RegisterAuthor("author-1", "Jane"))
        .shouldSucceed()
        .shouldHaveNoResponse();
}
var event = ChronicleCommandScenarios.chronicle(configured)
    .shouldHaveAppendedEvent("author-1", AuthorRegistered.class);
```

Run `./gradlew test --tests "*<SliceName>*"`. Fix all failures.

## Step 5 — Frontend (only if the command is UI-triggered)

After the backend builds, add `<Module>/<Feature>/<Slice>/<Component>.tsx` importing the proxy from
`./`, using `CommandDialog` / inline form. Add a barrel `index.ts`. Register it in the feature's
composition page. See the shared conventions doc's React section and
[references/patterns.md](references/patterns.md).

## Final verification — does the implementation match `slice.json`?

- [ ] Every `commands[]` field → a Command record property (no invented, none missing).
- [ ] Every `events[]` entry → an `@EventType` record; names match exactly; fields match.
- [ ] Every specification / GWT scenario → an executable spec.
- [ ] No business rule in `handle()` that is absent from the slice `description` / `comments`.
- [ ] `./gradlew build` is clean (0 warnings / 0 errors); slice specs pass.

## References
- [references/patterns.md](references/patterns.md) — full command/event/read-model-injection code,
  specs, and the React command UI patterns.
- [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the Cratis conventions.
