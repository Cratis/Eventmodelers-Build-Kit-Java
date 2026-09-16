---
title: Eventmodelers Build Kit - Java
description: Real-time Claude agent that connects to the Eventmodelers Platform and implements board slices as Cratis (Arc + Chronicle) vertical slices in a Java project
tableOfContents: false
---

# Eventmodelers Build Kit - Java

Real-time Claude agent that connects to the Eventmodelers Platform and implements board slices as Cratis (Arc + Chronicle) vertical slices in a Java project.

## What This Kit Does

When a slice is marked `Planned` on your Eventmodelers board, this kit automatically:

1. **Connects** to the Eventmodelers Platform in real-time
2. **Fetches** the slice definition and board context
3. **Generates** a complete Cratis vertical slice (commands, events, read models, React components)
4. **Builds** the slice with `./gradlew build` and runs tests with `./gradlew test`
5. **Updates** the board status to `Done` when complete

The entire process happens automatically, with your Claude agent implementing the slice according to Cratis best practices.

## Where to Start

- **[Getting Started](getting-started/)** — Install the kit and run your first slice from Planned to Done
- **[Install the Kit](getting-started/install.md)** — The installer, step by step

## How It Works

The kit implements the [Eventmodelers Build Kits platform contract](https://github.com/Nebulit-GmbH/Eventmodelers-Build-Kits):

1. **Real-time board subscription** — The kit subscribes to your board's slice status changes
2. **Status-based triggers** — Two independent loops:
   - `tasks.json` non-empty → run `prompt.md` (reacts to status changes)
   - Slice is `Planned` in `.slices/*/index.json` → run `backend-prompt.md` (builds one slice)
3. **Slice-type routing** — Automatically routes to the correct build skill:
   - `TRANSLATION` or `processors[]` → `build-automation` (reactors/automation)
   - `projections`/`queries`/`readModel` → `build-state-view` (read models)
   - Otherwise (`commands`/`events`) → `build-state-change` (commands/events)
4. **Cratis conformance** — Generated slices follow Cratis best practices:
   - One slice folder per slice (one public type per file) with `@Command` and `@EventType`
   - `ConceptAs<T>` for identities and values in events and read models
   - Read models exposed as Arc queries with `@Path`
   - Package mirrors the folder structure

## Place in the Cratis Ecosystem

This kit sits between the [Eventmodelers Platform](https://app.eventmodelers.ai) and [Cratis](https://cratis.dev):

- **Upstream**: [Eventmodelers Build Kits](https://github.com/Nebulit-GmbH/Eventmodelers-Build-Kits) — The platform that manages board slices
- **This Repository**: [Eventmodelers-Build-Kit-Java](https://github.com/Cratis/Eventmodelers-Build-Kit-Java) — The Java implementation
- **Downstream**: [Cratis Chronicle](https://github.com/Cratis/Chronicle.Kotlin) + [Cratis Arc](https://github.com/Cratis/Arc.Kotlin) — The frameworks the generated slices build on

See the sibling kits for other language implementations: [C#](https://github.com/Cratis/Eventmodelers-Build-Kit-CSharp) and [Kotlin](https://github.com/Cratis/Eventmodelers-Build-Kit-Kotlin).

## Installation

Install from git (this package is private, so use the git install path):

```bash
npx github:Cratis/Eventmodelers-Build-Kit-Java install
```

After installation, run:

```bash
node .build-kit/ralph-claude.js
```

## Next Steps

1. [Install the kit](getting-started/install.md)
2. Connect your Eventmodelers board credentials
3. Mark a slice as `Planned` on your board
4. Watch the kit automatically implement it as a Cratis vertical slice
