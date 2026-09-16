package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.arc.chronicle.TenantEventStoreResolver;
import io.cratis.chronicle.IEventStore;
import io.cratis.chronicle.java.ReadModelsJavaBridge;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/** Reads listings through the blocking Java bridge of the Chronicle client. */
public final class ChronicleListingReader implements ListingReader {
    private final TenantEventStoreResolver eventStoreResolver;

    public ChronicleListingReader(TenantEventStoreResolver eventStoreResolver) {
        this.eventStoreResolver = eventStoreResolver;
    }

    @Override
    public CompletionStage<List<Listing>> all(String namespace) {
        return CompletableFuture.supplyAsync(() ->
            ReadModelsJavaBridge.getInstances(eventStore(namespace).getReadModels(), Listing.class)
        );
    }

    private IEventStore eventStore(String namespace) {
        var eventStore = eventStoreResolver.resolve(namespace);
        if (eventStore == null) {
            throw new IllegalStateException("Chronicle event store is unavailable for tenant namespace '" + namespace + "'.");
        }
        return eventStore;
    }
}
