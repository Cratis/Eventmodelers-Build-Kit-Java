plugins {
    kotlin("jvm") version "2.4.20"
    java
    id("com.google.devtools.ksp") version "2.3.12"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.cratis"
version = "0.0.1"

dependencies {
    implementation("io.cratis:arc-chronicle-spring-boot-starter:7.2.0")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    ksp("io.cratis:arc-ksp:7.2.0")

    testImplementation("io.cratis:arc-testing:7.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// The Arc integration libraries are built against the coroutines version pinned in io.cratis:arc.
// Spring Boot's dependency management pins an older kotlinx-coroutines; keep the starter's runtime
// on the version the Cratis libraries were compiled with.
dependencyManagement {
    dependencies {
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.11.0")
    }
}

ksp {
    arg("arc.moduleName", "CratisApp")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// Slices live in <Module>/<Feature>/<Slice>/ folders at the project root (like the C# starter),
// tests mirror them under Tests/.
sourceSets {
    main {
        java.srcDir("SomeModule")
    }
    test {
        java.srcDir("Tests")
    }
}

tasks.test {
    useJUnitPlatform()
}
