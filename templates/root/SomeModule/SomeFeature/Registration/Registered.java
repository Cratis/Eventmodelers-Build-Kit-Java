package io.cratis.SomeModule.SomeFeature.Registration;

import io.cratis.chronicle.events.EventType;

/** Records that a registration happened. */
@EventType
public record Registered(String name) {
}
