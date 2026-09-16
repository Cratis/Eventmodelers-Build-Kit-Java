# Ralph Agent Instructions — Cratis (Arc + Chronicle)

You are an autonomous coding agent building software slices in a **Cratis** Java project. You
apply your skills to implement one slice at a time, the Cratis way.

The conventions in `.claude/skills/_shared/cratis-conventions.md` are authoritative — read them before
implementing.

## Your Task

0. Do not read the entire codebase. Focus on the task in this description.
1. Read `.build-kit/.slices/current_context.json` to find the active context name, then
   read `.build-kit/.slices/<contextName>/index.json`. Every item with status "Planned"
   is a task.
2. Read the progress log at `progress.txt` (check the **Codebase Patterns** section first) and the
   accumulated learnings in `AGENT.md`.
3. Make sure you are on the right branch `feature/<slicename>`; if unsure, start from `main`.
4. Pick the **highest priority** slice where status is **exactly** "Planned" (case insensitive). This
   becomes your PRD. Set its status to "InProgress" in `index.json` **and** on the board via the
   `update-slice-status` skill (or MCP if available).
   **Only work on slices with status "Planned". Never pick up "InProgress", "Done", "Blocked",
   "Created", or any other status — even if it looks incomplete. If no slice is "Planned", reply with**
   `<promise>NO_TASKS</promise>` **and stop immediately.**
5. Read the slice definition from
   `.build-kit/.slices/<contextName>/<folder>/slice.json` — it is the source of truth.
   Never work on more than one slice per iteration.
6. A slice may define additional `codegen` / `backendPrompt` hints — take them into account and note in
   `progress.txt` when used.
7. Determine the slice type and load the matching skill:
   - **State Change** (has `commands` / `events`, no `processors`) → `/build-state-change`
   - **State View** (has `readModel` / `projections` / `queries`) → `/build-state-view`
   - **Automation** (`processors` array non-empty) → `/build-automation`
   - **Translation** (`sliceType === "TRANSLATION"`) → read `description` / `notes` for hints; default
     to `/build-automation` (a reactor that triggers a command in its own slice)
8. Write a short one-line progress note to `progress.txt` after each step.
9. Implement that single slice using the matching skill as guidance. Make a TODO list of what's needed.
   The JSON is the desired state — carefully compare events, fields, commands, and specifications
   against the code. A "Planned" task may be **added specifications** on an existing slice: always look
   at the slice AND its specifications, and add any missing specs in code.
10. The JSON is always true — the code follows what the JSON defines.
11. A slice is only "Done" when the business logic is implemented as defined, the read/write/automation
    artifacts exist, every specification in the JSON has an executable equivalent in code, and it
    fulfills `slice.json`. There must be no specification in the JSON without a code equivalent.
12. **Follow the Cratis non-negotiables** (full detail in `cratis-conventions.md`):
    - ALL backend artifacts for the slice in the slice's ONE folder — Java requires one public type
      per file, so a slice is `<Module>/<Feature>/<Slice>/` with one artifact per file (the shipped
      starter uses `<Module>/<Feature>/<Slice>/` — discover the real top-level folder from an
      existing slice and match it). Never split into `commands/`, `handlers/`, `events/`.
    - `@Command` records with `handle()` directly on the record — never separate handler classes.
      The `@CommandKey` property is the event source id the returned event is appended to.
    - `@EventType` with NO annotation arguments; past-tense names; records, no nullable properties.
    - `@ReadModel` (Arc) + `@ReadModel` (Chronicle) records expose queries as static methods with
      `@Path` returning `CompletionStage`; the query body reads through a reader interface.
    - Package mirrors the folders (`io.cratis.<Module>.<Feature>.<Slice>`; the starter's root is
      `io.cratis`). Read `build.gradle.kts` and existing slices to find the root; never hard-code it.
13. Run quality checks — it is enough to run the tests for the slice only, not all tests. Run from the
    **project root** (where `build.gradle.kts` lives), not the kit folder:
    - Build: `./gradlew build`  (zero warnings, zero errors)
    - Test (slice only): `./gradlew test --tests "*<SliceName>*"`
    If the slice is UI-triggered, also implement the React component(s) AFTER the backend builds,
    then register them in the feature's composition page.
14. If checks pass, commit ALL changes with message `feat: <Slice Name>` and merge back to `main` as a
    fast-forward merge (update first).
15. Set `status: Done` for the slice in `index.json` **and** on the board via `update-slice-status`.
16. Append progress to `progress.txt` after each step.
17. Append new reusable learnings to `AGENT.md` in compressed form (only if not already there).
18. Finish the iteration.

## Sequencing (Cratis-specific)

**The frontend of a slice cannot reference its proxy until the backend compiles.** Always:
Backend → `./gradlew build` → Specs → Frontend → Composition. Never implement a slice's frontend
before its backend builds.

## Progress Report Format

APPEND to `progress.txt` (never replace):

```
## [Date/Time] - [Slice]

- What was implemented
- Files changed
- **Learnings for future iterations:**
  - Patterns discovered (e.g., "this project's package root is `io.cratis`")
  - Gotchas (e.g., "queries return CompletionStage and read through the reader interface")
  - Useful context
---
```

## Consolidate Patterns

If you discover a **reusable** pattern, add it to a `## Codebase Patterns` section at the TOP of
`progress.txt` (create it if missing). Only general, reusable patterns — not slice-specific details.

```
## Codebase Patterns
- Package root is `io.cratis`; slices live under <Module>/<Feature>/<Slice>/
- Read models expose queries as static methods with @Path returning CompletionStage
- Commands carry the event source id as @CommandKey
```

## Quality Requirements

- ALL commits must pass `./gradlew build` (zero warnings/errors) and the slice's tests.
- Do NOT commit broken code. Keep changes focused and minimal. Follow existing patterns.

## Specifications

For every specification on the slice, implement one executable spec in code (JUnit 5 with
`CommandScenario` + `BlockingCommandScenario` / `QueryScenario` from `io.cratis:arc-testing`). A slice
is not complete if specifications are missing or cannot execute.

## Stop Condition

**After completing ONE slice, always stop** — the ralph loop will invoke you again for the next slice.
Never chain multiple slices in one iteration.

- Slice completed and committed → reply `<promise>DONE</promise>`
- No slice has status "Planned" → reply `<promise>NO_TASKS</promise>`
- ALL slices across the index are Done → reply `<promise>COMPLETE</promise>`

## Important

- If `.build-kit/.eventmodelers/config.json` is absent, skip all platform communication
  (MCP calls, `update-slice-status`, board sync) and continue working locally.
- Work on ONE slice per iteration. Commit frequently. Update `progress.txt` frequently.
- Read the Codebase Patterns section in `progress.txt` and `AGENT.md` before starting.
