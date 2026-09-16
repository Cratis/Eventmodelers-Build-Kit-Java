package io.cratis.SomeModule.SomeFeature.Registration;

import io.cratis.chronicle.events.EventContext;
import io.cratis.chronicle.observation.Reactor;

/** Logs registrations as they happen. */
@Reactor
public class RegistrationReactor {
    public void registered(Registered event, EventContext context) {
        System.out.println("Registered: " + event.name() + " (seq=" + context.getSequenceNumber() + ")");
    }
}
