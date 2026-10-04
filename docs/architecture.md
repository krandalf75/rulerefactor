# RuleRefactor Architecture

## 1. Architecture Goals

- Provide a reusable refactoring engine for CLI, Agent, and SKILL usage.
- Distinguish clearly between issue detection and issue correction.
- Support selection by rule key and issue ID.
- Keep fixes deterministic, traceable, and safe.

## 2. Architectural Style

Hexagonal architecture (ports and adapters):

- **Core domain** defines contracts and orchestration.
- **Adapters** implement input/output channels (CLI, JSON API, external connectors).
- **Rule plugins** implement detection and correction capabilities.

## 3. Packaging and Namespaces (Single JAR)

The project uses a single executable JAR and package-level boundaries.

### 3.1 Package Layout

- `io.github.krandalf75.rulerefactor.core`
  - Domain models (`Issue`, `RuleKey`, `FixResult`, `ExecutionPlan`)
  - Use cases (`ListIssuesUseCase`, `PlanUseCase`, `ApplyUseCase`)
  - Service orchestration (`RefactorEngine`)
  - Port interfaces

- `io.github.krandalf75.rulerefactor.rules.detectors`
  - Code scanning logic with OpenRewrite visitors/recipes

- `io.github.krandalf75.rulerefactor.rules.fixers`
  - Automatic correction logic with OpenRewrite recipes and custom actions

- `io.github.krandalf75.rulerefactor.registry`
  - Rule capability registration and discovery

- `io.github.krandalf75.rulerefactor.adapters.cli`
  - Picocli commands: `list-issues`, `plan`, `apply`

- `io.github.krandalf75.rulerefactor.adapters.agent`
  - JSON input/output adapter for Agent/SKILL invocation

- `io.github.krandalf75.rulerefactor.adapters.connectors` (optional)
  - Adapters for external issue providers (issue ID and metadata sync)

## 4. Domain Model

### 4.1 Core Entities

- `RuleKey`: canonical rule identifier (`java:S106`).
- `IssueId`: external or internal issue ID.
- `CodeIssue`: detected issue in code.
- `FixAction`: executable code transformation.
- `FixOutcome`: result of applying a fix.

### 4.2 Supporting Objects

- `IssueSelector`: selection criteria by IDs, rule keys, severity, path.
- `ExecutionPlan`: selected issues + candidate actions.
- `ExecutionReport`: applied/skipped/failed results.

## 5. Detection vs Correction Interfaces

The project distinguishes two capabilities per rule.

### 5.1 Detection Interface

```java
public interface IssueDetector {
    RuleKey ruleKey();
    List<CodeIssue> detect(SourceSet sources, DetectionContext context);
}
```

### 5.2 Correction Interface

```java
public interface IssueFixer {
    RuleKey ruleKey();
    boolean supports(CodeIssue issue);
    FixOutcome apply(CodeIssue issue, MutableSourceSet sources, FixContext context);
}
```

### 5.3 Rule Capability Registry

```java
public interface RuleRegistry {
    Optional<IssueDetector> detector(RuleKey key);
    Optional<IssueFixer> fixer(RuleKey key);
    List<RuleCapability> capabilities();
}
```

Where `RuleCapability` indicates:
- detectable
- fixable
- fix confidence level (high/medium/low)

## 6. Execution Flows

### 6.1 List Issues

1. Load configured detectors.
2. Scan source set.
3. Return normalized issue list.

### 6.2 Plan

1. Detect issues.
2. Select by rule keys and/or issue IDs.
3. Resolve matching fixers.
4. Produce plan and expected impact.

### 6.3 Apply

1. Build plan.
2. Apply fixes in deterministic order.
3. Re-detect issues after each applied change block.
4. Re-parse and validate compilation state (if enabled).
5. Emit report and diff stats.

### 6.4 Incremental Selection and Reconciliation

When users select issues one by one (or a subset from a list), the apply engine must reconcile selection against the current code state before each apply step.

Execution loop:

1. Start from selected issue IDs and/or selected rule keys.
2. Detect current issues from filesystem state.
3. Match selected items against current issues.
4. Apply one deterministic fix step (or one rule-batch step).
5. Re-detect and continue until no selected pending items remain or max iterations is reached.

Matching strategy for selected issues:

- Primary key: `issueId` (when still present in current detection pass).
- Reconciliation key when IDs drift: `ruleKey + filePath + structural fingerprint`.
- If no match exists after reconciliation, mark as `skipped_outdated`.

This keeps user-controlled selection semantics while ensuring correctness when previous fixes shift lines or reshape AST nodes.

### 6.5 Apply Report Contract

`ExecutionReport` should include at least:

- `detected`: total currently detected before apply loop.
- `selected`: total requested by user.
- `applied`: successfully applied fix actions.
- `skipped`: skipped actions.
- `skipped_outdated`: selected issues no longer present at apply time.
- `failed`: fix attempts that errored.
- `entries[]`: per-selection trace (`issueRef`, `ruleKey`, `status`, `details`).

Status values:

- `applied`
- `skipped_not_fixable`
- `skipped_outdated`
- `failed`

## 7. Safety Strategy

- Deterministic transforms only.
- Optional dry-run before write.
- File-level rollback on failed transform.
- Idempotence checks (running twice should not add extra changes).
- Optional compile/test verification after apply.
- Max-iteration guard for iterative apply loops.
- Reconciliation-first apply (never apply on stale issue snapshots).

## 8. Agent/SKILL Integration

### 8.1 Adapter Contract

Input:
- execution mode
- project path
- selectors (`ruleKeys`, `issueIds`, query filters)
- dry-run flag

Output:
- detected issues
- selected issues
- applied/skipped/failed actions
- diagnostics and timing

### 8.2 Non-Interactive First

Agent mode should be fully non-interactive and scriptable.

## 9. Extensibility

- New rule = add detector and optional fixer implementation.
- Register in `RuleRegistry`.
- Add tests and capability metadata.

## 10. Testing Approach

- Unit tests for selectors and orchestration.
- Recipe tests for each fixer (before/after source).
- Integration tests for CLI and JSON adapter.
- Safety tests for idempotence and rollback.
