package io.cratis.SomeModule.SomeFeature;

import io.cratis.SomeModule.SomeFeature.Registration.Register;
import io.cratis.SomeModule.SomeFeature.Registration.Registered;
import io.cratis.arc.chronicle.ChronicleCommandScenarios;
import io.cratis.arc.generated.CratisAppArcArtifactModule;
import io.cratis.arc.testing.CommandScenario;
import io.cratis.arc.testing.java.BlockingCommandScenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Fast public-path contracts for the shipped example slice. */
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
