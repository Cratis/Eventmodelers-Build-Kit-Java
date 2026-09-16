# Automation / Translation Patterns (Java)

Concrete code for the `build-automation` skill. All snippets compile against the starter.

## 1. Automation — react with a side effect

```java
package io.cratis.SomeModule.SomeFeature.Registration;

import io.cratis.chronicle.events.EventContext;
import io.cratis.chronicle.observation.Reactor;

@Reactor
public class RegistrationReactor {
    public void registered(Registered event, EventContext context) {
        System.out.println("Registered: " + event.name() + " (seq=" + context.getSequenceNumber() + ")");
    }
}
```

## 2. Translation — event → event on another stream

```java
import io.cratis.chronicle.eventSequences.EventForEventSourceId;

@Reactor
public class AuditReactor {
    public EventForEventSourceId registered(Registered event, EventContext context) {
        return new EventForEventSourceId(
            "audit-log",
            new RegistrationAudited(context.getEventSourceId())
        );
    }
}
```

## 3. Same-stream follow-up event

```java
@Reactor
public class WelcomeReactor {
    public WelcomeEmailSent registered(Registered event, EventContext context) {
        return new WelcomeEmailSent(context.getEventSourceId());
    }
}
```

## 4. Multiple handlers in one reactor (same concern)

```java
@Reactor
public class RegistrationReactor {
    public void registered(Registered event, EventContext context) { /* … */ }
    public void registrationAmended(RegistrationAmended event, EventContext context) { /* … */ }
}
```

Dispatch is by the event parameter type — one method per event, named after the event.

## 5. Idempotency

Reactors may run again for the same event. Never assume once-only delivery: derive side effects from
the event payload (not from external mutable state), and make returned events self-describing.

## Checklist

- [ ] One reactor method per observed event, named after the event.
- [ ] Reactor is idempotent and stateless.
- [ ] Artifacts registered in `KnownClientArtifacts`.
- [ ] Spec per scenario in `slice.json`.
