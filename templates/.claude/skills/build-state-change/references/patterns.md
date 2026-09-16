# Write Slice Patterns (Java)

Concrete code for the `build-state-change` skill. All snippets compile against the starter
(`io.cratis:arc-chronicle-spring-boot-starter`). Java requires one public type per file — the
snippets below each live in their own file inside the slice folder.

## 1. Simplest write slice — single event

```java
package io.cratis.SomeModule.SomeFeature.Registration;

import io.cratis.chronicle.events.EventType;

@EventType
public record Registered(String name) {
}
```

```java
package io.cratis.SomeModule.SomeFeature.Registration;

import io.cratis.arc.artifacts.Command;
import io.cratis.arc.artifacts.CommandKey;
import io.cratis.arc.authorization.AllowAnonymous;

@Command
@AllowAnonymous
public record Register(@CommandKey String id, String name) {
    /** Produces the event appended to the command-key event source. */
    public Registered handle() {
        return new Registered(name);
    }
}
```

## 2. Asynchronous handler

```java
@Command
@AllowAnonymous
public record Register(@CommandKey String id, String name) {
    public CompletionStage<Registered> handle() {
        return CompletableFuture.completedFuture(new Registered(name));
    }
}
```

## 3. Business rule — inject a read model

```java
@Command
public record Register(@CommandKey String id, String name) {
    public Registered handle(AuthorByName existing) {
        if (existing != null) throw new IllegalStateException("Name already taken");
        return new Registered(name);
    }
}
```

Only encode rules that appear in the slice `description` / `comments`.

## 4. Spec — CommandScenario (from `io.cratis:arc-testing`)

```java
package io.cratis.SomeModule.SomeFeature;

import io.cratis.SomeModule.SomeFeature.Registration.Register;
import io.cratis.SomeModule.SomeFeature.Registered;
import io.cratis.arc.chronicle.ChronicleCommandScenarios;
import io.cratis.arc.generated.CratisAppArcArtifactModule;
import io.cratis.arc.testing.CommandScenario;
import io.cratis.arc.testing.java.BlockingCommandScenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationTests {
    private final CratisAppArcArtifactModule module = new CratisAppArcArtifactModule();

    @Test
    void registerAppendsTheRegisteredEvent() {
        var configured = new CommandScenario<>(module, Register.class);
        try (var scenario = new BlockingCommandScenario<>(configured)) {
            scenario.execute(new Register("listing-1", "Some Name"))
                .shouldSucceed()
                .shouldHaveNoResponse();
        }
        var event = ChronicleCommandScenarios.chronicle(configured)
            .shouldHaveAppendedEvent("listing-1", Registered.class);
        assertEquals("Some Name", event.name());
    }
}
```

## 5. React command UI (after the backend built)

```tsx
import { CommandDialog } from '@cratis/components/CommandDialog';
import { InputTextField } from '@cratis/components/CommandForm';
import { Register } from './Register';

export const RegisterDialog = () => (
    <CommandDialog command={Register} title='Register' okLabel='Register'>
        <InputTextField<Register> value={c => c.name} title='Name' />
    </CommandDialog>
);
```

## Checklist

- [ ] Events past tense, `@EventType` with no arguments, records, never nullable.
- [ ] `@CommandKey` carries the event source id.
- [ ] `handle()` on the command record — never a separate handler.
- [ ] One public type per file inside the slice folder.
- [ ] Spec per scenario in `slice.json`.
