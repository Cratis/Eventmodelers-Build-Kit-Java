# Read Slice Patterns (Java)

Concrete code for the `build-state-view` skill. All snippets compile against the starter. Java
requires one public type per file — the snippets below each live in their own file inside the slice
folder.

## 1. Read model

```java
package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.arc.authorization.AllowAnonymous;
import io.cratis.chronicle.readModels.ReadModel;

@io.cratis.arc.artifacts.ReadModel
@ReadModel
@AllowAnonymous
public record Listing(String id, String name) {
}
```

## 2. Reader with the Arc query

```java
package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.arc.artifacts.FromServices;
import io.cratis.arc.queries.Path;
import io.cratis.arc.queries.QueryContext;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface ListingReader {
    @Path("/api/some-module/some-feature/listing/all-listings")
    static CompletionStage<List<Listing>> allListings(
        QueryContext context,
        @FromServices ListingReader reader
    ) {
        return reader.all(requireNamespace(context));
    }

    CompletionStage<List<Listing>> all(String namespace);

    private static String requireNamespace(QueryContext context) {
        var namespace = context.getTenantNamespace();
        if (namespace == null) throw new IllegalStateException("A tenant namespace is required.");
        return namespace;
    }
}
```

## 3. Chronicle-backed reader implementation

```java
package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.arc.chronicle.TenantEventStoreResolver;
import io.cratis.chronicle.IEventStore;
import io.cratis.chronicle.java.ReadModelsJavaBridge;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
            throw new IllegalStateException("Chronicle event store is unavailable for namespace '" + namespace + "'.");
        }
        return eventStore;
    }
}
```

Register the reader bean in the application's Chronicle configuration.

## 4. Reducer (running state)

```java
package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.SomeModule.SomeFeature.Registration.Registered;
import io.cratis.chronicle.events.EventContext;
import io.cratis.chronicle.observation.Reducer;

@Reducer
public final class ListingReducer {
    public Listing registered(Registered event, Listing state, EventContext context) {
        return new Listing(context.getEventSourceId(), event.name());
    }
}
```

## 5. Spec — QueryScenario

```java
var byId = module.getQueryPerformers().stream()
    .filter(p -> p.getFullyQualifiedName().getValue().endsWith(".allListings"))
    .findFirst().orElseThrow();
try (var scenario = new BlockingQueryScenario<Listing>(new QueryScenario<Listing>(byId)
        .addService(ListingReader.class, reader)
        .withTenant("tenant-a"))) {
    scenario.perform(Map.of())
        .shouldSucceed()
        .shouldHaveData(expected);
}
```

## 6. React — render the query proxy

```tsx
import { AllListings } from './AllListings';

export const ListingDataTable = () => {
    const [result] = AllListings.use();
    return <DataTable value={result.data} />;
};
```

## Checklist

- [ ] Read model records, no nullable properties.
- [ ] Query is a static method with `@Path` returning `CompletionStage`, reading through the reader.
- [ ] Reducer/projection joins events, never read models.
- [ ] Spec per scenario in `slice.json`.
