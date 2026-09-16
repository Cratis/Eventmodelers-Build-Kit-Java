---
name: build-state-view
description: >
  Implement read slices the Cratis way — an @ReadModel record fed by a Chronicle reducer or
  projection, exposed as an Arc query, plus the React component that renders it. Use when:
  (1) implementing a new read slice / projection in a Cratis project, (2) a slice.json has a
  non-empty readModel / projections / queries section, (3) the user provides a read-slice Event
  Modeling artifact or specification and asks to implement it, (4) the user says "implement",
  "create", "add" a read slice, read model, projection, reducer, or query in a Cratis Arc /
  Chronicle project.
---

# Cratis — Read Slice (State View)

A read slice projects events into a queryable read model. In Cratis the path lives in **one folder**
(one public type per file):

```
<Module>/<Feature>/<Slice>/
├── <ReadModel>.java     ← @ReadModel record
├── <Reader>.java        ← reader interface + @Path query
├── <Reducer>.java       ← @Reducer building the read model
└── <Component>.tsx      ← React table using the proxy
```

> **Read first:** [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md). Everything
> below assumes those rules.

## Step 0 — Discover conventions

Read the project's `CLAUDE.md` and at least one existing read slice. Confirm the package root, how
read models expose queries, and how reducers are shaped.

## Step 1 — Understand the input (`slice.json` is the source of truth)

| Element | What to extract |
|---|---|
| **Read model** | Name, fields (from the events it is built from) |
| **Projections / queries** | Which events feed it, which queries expose it |
| **Specifications** | Each GWT / scenario maps 1:1 to an executable spec |

**If a field is not derivable from `slice.json` and the events, it does not go in the read model.**

## Step 2 — Write the read model and reader

`<Module>/<Feature>/<Slice>/` — package `io.cratis.<Module>.<Feature>.<Slice>`:

```java
import io.cratis.arc.authorization.AllowAnonymous;
import io.cratis.chronicle.readModels.ReadModel;

@io.cratis.arc.artifacts.ReadModel
@ReadModel
@AllowAnonymous
public record Listing(String id, String name) {
}
```

The reader interface carries the query and reads through the Chronicle read-model service:

```java
import io.cratis.arc.artifacts.FromServices;
import io.cratis.arc.chronicle.TenantEventStoreResolver;
import io.cratis.arc.queries.Path;
import io.cratis.arc.queries.QueryContext;
import io.cratis.chronicle.IEventStore;
import io.cratis.chronicle.java.ReadModelsJavaBridge;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface ListingReader {
    @Path("/api/<module>/<feature>/<slice>/all-listings")
    static CompletionStage<List<Listing>> allListings(
        QueryContext context,
        @FromServices ListingReader reader
    ) {
        return reader.all(requireNamespace(context));
    }

    CompletionStage<List<Listing>> all(String namespace);

    private static String requireNamespace(QueryContext context) {
        var namespace = context.getTenantNamespace();
        if (namespace == null) throw new IllegalStateException("A tenant namespace is required.");
        return namespace;
    }
}
```

Register the Chronicle-backed reader bean in the application's Chronicle configuration.

## Step 3 — Write the reducer (or projection)

```java
import io.cratis.chronicle.events.EventContext;
import io.cratis.chronicle.observation.Reducer;

@Reducer
public final class ListingReducer {
    public Listing registered(Registered event, Listing state, EventContext context) {
        return new Listing(context.getEventSourceId(), event.name());
    }
}
```

- One method per event, named after the event; `state` is the current read model (null on first
  event); return the **new** state.
- Reducers join events, never read models. For declarative projections use `@Projection` +
  `IProjectionFor<T>` (see [references/patterns.md](references/patterns.md)).
- Remember to add new artifacts to the `KnownClientArtifacts` list in the Chronicle configuration.

## Step 4 — Build

From the project root: `./gradlew build`. Fix ALL warnings and errors.

## Step 5 — Write specs

Use `QueryScenario` with `BlockingQueryScenario` from `io.cratis:arc-testing` in
`Tests/<Module>/<Feature>/<SliceName>Tests.java` — one per scenario in `slice.json` (happy path, each
projection rule, each query). Run `./gradlew test --tests "*<SliceName>*"`.

## Step 6 — Frontend

After the backend builds, add the proxy import and `<Module>/<Feature>/<Slice>/<Component>.tsx`:
a DataTable using the observable query hook, fed by the generated proxy. Add a barrel `index.ts` and
register the component in the feature's composition page. See
[references/patterns.md](references/patterns.md).

## Final verification — does the implementation match `slice.json`?

- [ ] Read model fields match the events it is built from (no invented, none missing).
- [ ] Every `queries[]` entry → an executable query with `@Path`.
- [ ] Every `projections[]` / event mapping → a reducer method (or projection `from` clause).
- [ ] Every specification → an executable spec.
- [ ] `./gradlew build` is clean; slice specs pass.

## References
- [references/patterns.md](references/patterns.md) — reducer, projection, reader and React table
  patterns.
- [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the Cratis conventions.
