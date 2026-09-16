package io.cratis.SomeModule.SomeFeature.Listing;

import io.cratis.arc.authorization.AllowAnonymous;
import io.cratis.chronicle.readModels.ReadModel;

/** Registrations materialized by Chronicle and exposed through generated Arc queries. */
@io.cratis.arc.artifacts.ReadModel
@ReadModel
@AllowAnonymous
public record Listing(String id, String name) {
}
