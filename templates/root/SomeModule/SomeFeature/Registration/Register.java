package io.cratis.SomeModule.SomeFeature.Registration;

import io.cratis.arc.artifacts.Command;
import io.cratis.arc.artifacts.CommandKey;
import io.cratis.arc.authorization.AllowAnonymous;

/** Registers something by name. */
@Command
@AllowAnonymous
public record Register(@CommandKey String id, String name) {
    /** Produces the event appended to the command-key event source. */
    public Registered handle() {
        return new Registered(name);
    }
}
