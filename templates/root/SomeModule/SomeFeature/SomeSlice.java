package io.cratis.SomeModule.SomeFeature;

import io.cratis.chronicle.client.java.*;

/**
 * Example slice demonstrating the Cratis way in Java.
 * 
 * This is a state change slice that implements a command.
 * Follow this pattern for all slices.
 */
@Command
public class SomeSlice {
    
    public record Register(java.util.UUID id, String name) {}
    
    public record Handle() implements IReactor<Register> {
        public void invoke(Register command, IEventContext context) {
            // Implement your command handling logic here
            context.publish(new SomeEvent(command.id(), command.name()));
        }
    }
    
    public record SomeEvent(java.util.UUID id, String name) implements IEvent {}
}
