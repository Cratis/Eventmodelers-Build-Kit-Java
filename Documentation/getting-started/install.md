---
title: Install the Kit
description: Install the Eventmodelers Build Kit - Java into your project directory
tableOfContents: false
---

# Install the Kit

This tutorial shows you how to install the Eventmodelers Build Kit - Java into your project directory.

## Prerequisites

Before installing, ensure you have:

- **Node.js 18+** installed ([Download](https://nodejs.org/))
- **JDK 17+** installed ([Download](https://adoptium.net/))
- **Claude Code** or another Claude-compatible agent ([Get Claude](https://claude.ai))
- **Eventmodelers account** with at least one board ([Sign up](https://app.eventmodelers.ai/account))

## Installation

### Option 1: Direct Git Install (Recommended)

Install directly from this repository:

```bash
npx github:Cratis/Eventmodelers-Build-Kit-Java install
```

This will:

1. Copy the **Cratis starter app** (Java backend with Arc + Chronicle, example slice, shared React frontend) into your project root
2. Create a `.build-kit/` directory with the agent loop and skills
3. Install Node.js dependencies for the loop
4. Add `.build-kit/` to your `.gitignore`
5. Create `.eventmodelers/config.json` in your project root
6. Configure MCP server in `.build-kit/.claude/settings.json`

### Option 2: Upstream CLI with Git Stack

Use the upstream Eventmodelers CLI to install this repository as a git stack:

```bash
npx @eventmodelers/cli init --stack cratis-java --git https://github.com/Cratis/Eventmodelers-Build-Kit-Java
```

This achieves the same result but uses the upstream CLI's stack management.

## During Installation

The installer prompts for your Eventmodelers credentials:

```
🔑 Enter your Eventmodelers credentials (press Enter to skip any field):

  Organization ID:
  Board ID:
  Token:
```

Get these from [app.eventmodelers.ai/account](https://app.eventmodelers.ai/account). You can skip
them and configure later — see the `connect` skill.

## After Installation

Start the agent loop:

```bash
node .build-kit/ralph-claude.js
```

Verify the installation:

```bash
npx github:Cratis/Eventmodelers-Build-Kit-Java status
```

Build and run the starter:

```bash
docker-compose up -d   # Chronicle development kernel
./gradlew build        # backend
./gradlew bootRun      # run the backend (http://localhost:8080)
npm install && npm run dev   # frontend dev server
```
