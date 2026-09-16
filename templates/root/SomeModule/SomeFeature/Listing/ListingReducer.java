package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.SomeModule.SomeFeature.Registration.Registered;
import io.cratis.chronicle.events.EventContext;
import io.cratis.chronicle.observation.Reducer;

/** Builds {@link Listing} from {@link Registered} events. */
@Reducer
public final class ListingReducer {
    /** Creates the listing state from the first registered event. */
    public Listing registered(Registered event, Listing state, EventContext context) {
        return new Listing(context.getEventSourceId(), event.name());
    }
}
