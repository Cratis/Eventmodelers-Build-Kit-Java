package io.cratis;

import io.cratis.SomeModule.SomeFeature.Listing.ChronicleListingReader;
import io.cratis.SomeModule.SomeFeature.Listing.Listing;
import io.cratis.SomeModule.SomeFeature.Listing.ListingReader;
import io.cratis.SomeModule.SomeFeature.Listing.ListingReducer;
import io.cratis.SomeModule.SomeFeature.Registration.Register;
import io.cratis.SomeModule.SomeFeature.Registration.Registered;
import io.cratis.SomeModule.SomeFeature.Registration.RegistrationReactor;
import io.cratis.arc.chronicle.TenantEventStoreResolver;
import kotlin.reflect.KClass;
import io.cratis.chronicle.ChronicleOptions;
import io.cratis.chronicle.artifacts.KnownClientArtifacts;
import io.cratis.chronicle.connection.ChronicleConnectionString;
import io.cratis.chronicle.spring.ChronicleProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static kotlin.jvm.JvmClassMappingKt.getKotlinClass;

/** Bridges Arc and Chronicle for the shipped example slices. */
@Configuration(proxyBeanMethods = false)
public class ChronicleConfiguration {
    /** Uses an explicit artifact list so executable Spring Boot jars register the same domain contract. */
    @Bean
    public ChronicleOptions chronicleOptions(
        ChronicleProperties properties,
        @Value("${spring.application.name:Unknown}") String applicationName
    ) {
        return new ChronicleOptions(
            ChronicleConnectionString.Companion.parse(properties.getConnectionString()),
            properties.getProgramIdentifier() != null ? properties.getProgramIdentifier() : applicationName,
            properties.getDefaultSinkTypeId() != null
                ? properties.getDefaultSinkTypeId()
                : System.getenv().getOrDefault("CHRONICLE_SINK_TYPE", "InMemory"),
            properties.getAutoDiscoverAndRegister(),
            new KnownClientArtifacts(
                kotlinClass(Register.class),
                kotlinClass(Registered.class),
                kotlinClass(RegistrationReactor.class),
                kotlinClass(Listing.class),
                kotlinClass(ListingReducer.class)
            )
        );
    }

    private static KClass<?> kotlinClass(Class<?> clazz) {
        return getKotlinClass(clazz);
    }

    /** Supplies generated queries with explicit namespace-aware Chronicle reads. */
    @Bean
    public ListingReader listingReader(TenantEventStoreResolver resolver) {
        return new ChronicleListingReader(resolver);
    }
}

