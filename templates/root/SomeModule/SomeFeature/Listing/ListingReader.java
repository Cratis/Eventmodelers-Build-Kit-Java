package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.arc.artifacts.FromServices;
import io.cratis.arc.chronicle.TenantEventStoreResolver;
import io.cratis.arc.queries.Path;
import io.cratis.arc.queries.QueryContext;
import java.util.List;
import java.util.concurrent.CompletionStage;

/** Reads listings from one explicit Chronicle namespace and exposes them as an Arc query. */
public interface ListingReader {
    /** Gets every listing in the namespace captured in Arc's query context. */
    @Path("/api/some-module/some-feature/listing/all-listings")
    static CompletionStage<List<Listing>> allListings(
        QueryContext context,
        @FromServices ListingReader reader
    ) {
        return reader.all(requireNamespace(context));
    }

    /** Gets every listing in the namespace. */
    CompletionStage<List<Listing>> all(String namespace);

    private static String requireNamespace(QueryContext context) {
        var namespace = context.getTenantNamespace();
        if (namespace == null) throw new IllegalStateException("A tenant namespace is required.");
        return namespace;
    }
}
