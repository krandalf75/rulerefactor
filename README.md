# RuleRefactor

RuleRefactor is a Java executable that detects and fixes code quality issues through automated, rule-based refactoring.

The project is designed to be reusable from:
- a command-line interface (CLI)
- an AI Agent
- a SKILL workflow

It supports selecting fixes by rule type (for example, `java:S106`) or by issue identifier (when integrated with a quality platform).

## Objectives

- Reduce technical debt quickly and safely.
- Apply deterministic refactorings with low regression risk.
- Separate issue detection from issue correction.
- Provide a stable engine that can be called by humans or AI.

## Core Concepts

- **Rule Catalog**: defines which rules can be detected and/or fixed automatically.
- **Detection Engine**: scans source code and returns detected issues.
- **Correction Engine**: applies code changes for selected issues.
- **Execution Modes**:
  - `list-issues`: detect and list issues
  - `plan`: show proposed changes without modifying files
  - `apply`: apply selected fixes

## High-Level Architecture

Single executable JAR, organized by Java packages:

- `io.github.krandalf75.rulerefactor.core`: domain model, orchestration, interfaces.
- `io.github.krandalf75.rulerefactor.rules.detectors`: rule detectors (AST/LST-based).
- `io.github.krandalf75.rulerefactor.rules.fixers`: correction recipes and fix actions.
- `io.github.krandalf75.rulerefactor.adapters.cli`: CLI adapter.
- `io.github.krandalf75.rulerefactor.adapters.agent`: JSON adapter for Agent/SKILL integration.
- `io.github.krandalf75.rulerefactor.adapters.connectors` (optional): external issue source connectors.

Detailed architecture is documented in `docs/architecture.md`.

## Suggested Technology Stack

- Java 21 (LTS)
- OpenRewrite (AST/LST and recipes)
- Picocli (CLI)
- Jackson (JSON input/output)
- SLF4J + Logback (logging)
- JUnit 5 + AssertJ (tests)
- Maven (build and packaging)

Runtime requirement:
- RuleRefactor supports only Java 21.
- The application validates the runtime at startup and exits with an error if Java 21 is not used.

## Packaging Strategy

- One Maven project.
- One deployable artifact: `rulerefactor.jar`.
- Internal organization by package boundaries, not by Maven modules.

## Build and Run

Build with Maven Wrapper:

```bash
./mvnw clean test package
```

Run with default Java:

```bash
java -jar target/rulerefactor-0.1.0.jar --help
```

## Automated releases

Pushing a tag in the form `vX.Y.Z` starts the GitHub Actions release workflow. It runs `./mvnw clean verify`, checks that the packaged application's version matches the tag, and publishes both `rulerefactor-X.Y.Z.jar` and `rulerefactor.jar` to the GitHub Release. The stable filename is used by the `java-code-doctor` skill.

Before creating a release, update the Maven version in `pom.xml` and the CLI version in `RuleRefactorCommand.java` to the same value, then commit the changes. For example:

```bash
git tag -a v0.1.1 -m "Release v0.1.1"
git push origin v0.1.1
```

## Troubleshooting

- Ensure `java -version` reports Java 21 before running the CLI.
- If wrapper dependencies fail to download, retry `./mvnw -U clean test package`.

## Rule Model: Detection vs Correction

RuleRefactor explicitly separates:

1. **Detection rules** (find problems)
2. **Correction actions** (fix problems)

Not every detected rule must be auto-fixable. A rule can be:
- detectable only
- detectable and fixable
- externally sourced and fixable

This is implemented using dedicated interfaces per action (see `docs/architecture.md`).

## Apply Semantics (Selection-Safe)

`apply` is designed to support user-driven selection from an issue list (including one-by-one execution) without relying on stale line numbers.

- Selection input supports `issueIds` and/or `ruleKeys`.
- The engine re-detects issues during apply iterations.
- Before each step, selected items are reconciled against current code state.
- If a selected item is no longer present, it is reported as `skipped_outdated` (not treated as failure).

This makes sequential fix workflows robust when earlier fixes shift code structure.

## Example CLI

```bash
# List all detectable issues
java -jar rulerefactor.jar list-issues --path . --format table

# Filter by rule keys
java -jar rulerefactor.jar list-issues --path . --rules java:S106,java:S1481 --format json

# Preview fixes without writing files
java -jar rulerefactor.jar plan --path . --rules java:S106

# Apply fixes for selected issue IDs
java -jar rulerefactor.jar apply --path . --issue-ids ISSUE-1001,ISSUE-1044

# Apply fixes and run verification hooks after each change
java -jar rulerefactor.jar apply --path . --rules java:S2293 --verify compile,test

# Non-interactive agent API from JSON file
java -jar rulerefactor.jar agent-api --input docs/request.json
```

## JSON Contract for Agent/SKILL

Request (example):

```json
{
  "projectPath": ".",
  "mode": "apply",
  "selection": {
    "ruleKeys": ["java:S106"],
    "issueIds": []
  },
  "dryRun": false
}
```

Response (example):

```json
{
  "detected": 12,
  "selected": 4,
  "applied": 3,
  "skipped": 1,
  "errors": []
}
```

## Documentation

- Architecture: `docs/architecture.md`
- Implementation plan: `docs/implementation-plan.md`
- Detectable Java SonarQube rules: `docs/detectable-java-rules.md`
- Agent API schema: `docs/agent-api-schema.json`
- Agent API examples: `docs/agent-api-examples.md`
- Rule onboarding template: `docs/rule-onboarding-template.md`

## Codex Skill

The reusable `java-code-doctor` skill is available under `skills/java-code-doctor` and can be installed in Codex with:

```text
$skill-installer install https://github.com/krandalf75/rulerefactor/tree/main/skills/java-code-doctor
```

The skill uses Java 21 and downloads the standalone JAR from the latest GitHub Release when no local JAR is configured.

## Status

- Detection and fix pipeline implemented for 23 Java rules.
- Selection-safe iterative apply with stale-selection reconciliation.
- Verification hooks available on apply (`compile`, `test`) with rollback on verification failure.
