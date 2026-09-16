---
name: build-automation
description: >
  Implement automation and translation slices the Cratis way — a @Reactor that observes events and
  produces side effects, optionally returning new events for the same or another event source. Use
  when: (1) implementing a new automation / reactor in a Cratis project, (2) a slice.json has a
  non-empty processors[] section or sliceType === "TRANSLATION", (3) the user provides an Event
  Modeling artifact or description of an event-driven reaction and asks to implement it, (4) the user
  says "implement", "create", "add" an automation, reactor, translation, or event-to-command flow in
  a Cratis Arc / Chronicle project.
---

# Cratis — Automation / Translation Slice

An automation *reacts* to events and *does things* (side effects). A translation adapts an event by
triggering a command in its own slice. Both are implemented with a **`@Reactor`**.

```
event  →  @Reactor method (dispatch by parameter type)  →  side effect / returned event
```

> **Read first:** [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md). Everything
> below assumes those rules.

## Step 0 — Discover conventions

Read the project's `CLAUDE.md` and at least one existing slice. Confirm the package root and how
reactors are shaped.

## Step 1 — Understand the input (`slice.json` is the source of truth)

| Element | What to extract |
|---|---|
| **Trigger** | Which event(s) the processor observes |
| **Effect** | What happens: returned event (same stream), `EventForEventSourceId` (other stream), external side effect, or a command |
| **Specifications** | Each GWT / scenario maps 1:1 to an executable spec |

**If the effect is not in `slice.json`, it does not go in the code.**

## Step 2 — Write the reactor

`<Module>/<Feature>/<Slice>/<Slice>Reactor.java` — package `io.cratis.<Module>.<Feature>.<Slice>`:

```java
import io.cratis.chronicle.events.EventContext;
import io.cratis.chronicle.eventSequences.EventForEventSourceId;
import io.cratis.chronicle.observation.Reactor;

@Reactor
public class RegistrationReactor {
    /** External side effect — return nothing. */
    public void registered(Registered event, EventContext context) {
        System.out.println("Registered: " + event.name() + " (seq=" + context.getSequenceNumber() + ")");
    }

    /** Same-stream side effect via a bare return. */
    public WelcomeEmailSent registeredWelcome(Registered event, EventContext context) {
        return new WelcomeEmailSent(context.getEventSourceId());
    }

    /** Cross-stream side effect via {@link EventForEventSourceId}. */
    public EventForEventSourceId registeredAudit(Registered event, EventContext context) {
        return new EventForEventSourceId("audit-log", new RegistrationAudited(context.getEventSourceId()));
    }
}
```

- One method per observed event, named after the event; dispatch is by the parameter type.
- Reactors must be **idempotent and stateless** — they may run again for the same event.
- A *translation* is a reactor that returns the event(s) of the slice it belongs to; a command in the
  same slice is the Arc-native alternative when validation is needed.
- Remember to add the reactor to the `KnownClientArtifacts` list in the Chronicle configuration.

## Step 3 — Build

From the project root: `./gradlew build`. Fix ALL warnings and errors.

## Step 4 — Write specs

Specs live in `Tests/<Module>/<Feature>/<SliceName>Tests.java` — one per scenario in `slice.json`:
the reactor emits/does the expected effect for the observed event, and is idempotent under replay.
Run `./gradlew test --tests "*<SliceName>*"`.

## Step 5 — Frontend (only if the slice is UI-visible)

Automations rarely have UI. If the slice produces state another component shows, that component
belongs to the consuming state-view slice — implement it there.

## Final verification — does the implementation match `slice.json`?

- [ ] Every observed event → a reactor method with the event as first parameter.
- [ ] Every `processors[]` effect → a returned event, `EventForEventSourceId`, or external effect.
- [ ] Reactor is idempotent and stateless.
- [ ] Every specification → an executable spec.
- [ ] `./gradlew build` is clean; slice specs pass.

## References
- [references/patterns.md](references/patterns.md) — reactor, translation and side-effect patterns.
- [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the Cratis conventions.
