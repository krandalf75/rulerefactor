# RuleRefactor Implementation Plan

## Phase 0 - Foundations (Week 1)

Goals:
- Bootstrap single-project Java codebase.
- Define contracts for detection and correction.
- Deliver a minimal CLI skeleton.

Deliverables:
- Single Maven project producing one executable JAR.
- Base package structure (`io.github.krandalf75.rulerefactor.*`).
- Core interfaces (`IssueDetector`, `IssueFixer`, `RuleRegistry`).
- Commands: `list-issues`, `plan`, `apply` (stub behavior).
- Basic JSON request/response DTOs.

Acceptance criteria:
- Project builds with `mvn test`.
- CLI runs and prints structured output.

Checks:
- [x] Single Maven project configured (`pom.xml`).
- [x] Executable shaded JAR packaging works.
- [x] Base package structure created under `io.github.krandalf75.rulerefactor.*`.
- [x] Core interfaces added (`IssueDetector`, `IssueFixer`, `RuleRegistry`).
- [x] CLI skeleton and commands added (`list-issues`, `plan`, `apply`).
- [x] Build verified with `./mvnw clean test package`.
- [x] CLI execution verified with `java -jar ... --help`.
- [ ] Basic JSON request/response DTOs.

## Phase 1 - Detection MVP (Week 2)

Goals:
- Implement issue detection pipeline with OpenRewrite.
- Provide stable issue model and filtering.

Deliverables:
- `RefactorEngine` detection flow.
- `IssueSelector` filtering by rule key and issue ID.
- Initial detector set (for example 5-10 rules).

Acceptance criteria:
- `list-issues` returns deterministic results in table and JSON.
- Filtering by `--rules` works.

Checks:
- [x] `RefactorEngine` detection flow implemented.
- [x] `IssueSelector` filtering by rule key and issue ID implemented.
- [x] Initial detector set implemented (5-10 rules).
- [x] `list-issues --format table|json` produces real issue output.
- [x] `--rules` filtering validated with tests.

## Phase 2 - Correction MVP (Week 3)

Goals:
- Implement automated fixes for first rule batch.
- Add dry-run and report output.

Deliverables:
- Fixer implementations for first rules.
- Plan generation with capability checks.
- Apply pipeline with write/no-write modes.
- Iterative apply loop with re-detection and stale-selection handling.

Acceptance criteria:
- `plan` lists fix actions per issue.
- `apply --dry-run` shows change summary without file writes.
- `apply` modifies files and emits applied/skipped/failed counts.
- Applying selected issues one-by-one remains correct even if previous fixes move code.

Checks:
- [x] First fixer implementations added.
- [x] Plan generation with capability checks implemented.
- [x] Apply pipeline supports dry-run and write modes.
- [x] `plan` command returns per-issue fix actions.
- [x] `apply` command returns applied/skipped/failed report.
- [x] Apply loop re-detects after each step and reconciles stale selections.
- [x] Outdated selected issues are reported as `skipped_outdated`.

### Phase 2.1 - Wave 1 Fixers (Incremental)

Goals:
- Implement safe autofix for Wave 1 rules with deterministic order and iterative reconciliation.

Wave 1 scope:
- `java:S1128`, `java:S1155`, `java:S2293`, `java:S6204`, `java:S1905`, `java:S1481`, `java:S1118`, `java:S1602`, `java:S2111`

Execution strategy:
1. User selects by `issueIds` and/or `ruleKeys`.
2. Engine detects current issues.
3. Engine resolves fixable selected items.
4. Engine applies next safe action.
5. Engine re-detects and reconciles remaining selection.
6. Repeat until convergence or max-iterations.

Checks:
- [x] Registry marks Wave 1 rules as `fixable=true` where fixer exists.
- [x] Fixers are idempotent for Wave 1.
- [x] Per-issue apply trace is emitted (`applied/skipped_outdated/failed`).
- [x] Convergence tests: repeated run yields zero extra changes.

## Phase 3 - Safety and Verification (Week 4)

Goals:
- Reduce regression risk.
- Validate transformed code.

Deliverables:
- Idempotence tests for implemented fixers.
- Optional verification hooks (`compile`, `test`).
- Rollback strategy for failed file transforms.

Acceptance criteria:
- Re-running apply does not produce duplicate changes.
- Failed transformations do not leave partial broken state.

Checks:
- [x] Idempotence tests added for implemented fixers.
- [x] Verification hooks added (`compile`, `test`).
- [x] Rollback strategy implemented for failed transforms.
- [x] Re-run apply behavior validated.

## Phase 4 - Agent/SKILL Integration (Week 5)

Goals:
- Expose stable non-interactive JSON interface.

Deliverables:
- `rule-refactor-agent-api` adapter.
- JSON schema documentation.
- Example requests for selection by rule and by issue ID.

Acceptance criteria:
- Agent can invoke engine with a single JSON payload.
- Response includes detected/selected/applied/skipped/failed.

Checks:
- [x] Agent adapter implemented (`adapters.agent`).
- [x] JSON schema documented.
- [x] Example payloads for selection by rule and issue ID added.
- [x] End-to-end Agent invocation validated.

## Phase 5 - Rule Expansion (Week 6+)

Goals:
- Expand supported rules safely and incrementally.

Deliverables:
- Rule onboarding template.
- Additional rule detectors/fixers.
- Confidence tagging per fixer.

Acceptance criteria:
- Each new rule has detector/fixer tests.
- Rule catalog reports capability and confidence.

Checks:
- [x] Rule onboarding template published.
- [x] Capability/confidence metadata enforced in registry.
- [x] New rules include detector/fixer/idempotence tests.

## Rule Onboarding Workflow

For each rule:
1. Define metadata (`ruleKey`, title, severity hint, autofixable).
2. Implement detector.
3. Implement fixer (if safe).
4. Add before/after tests.
5. Add idempotence test.
6. Register capability in registry.

## Initial Backlog (Recommended)

- Build and package scaffolding.
- Engine orchestration.
- Issue model + selectors.
- 10 initial low-risk rule implementations.
- CLI UX polishing and JSON output stabilization.
- CI pipeline: test + quality checks.

## Risks and Mitigations

- Risk: false positives in detection.
  - Mitigation: strict detection predicates + fixtures.
- Risk: unsafe auto-fixes.
  - Mitigation: confidence levels and opt-in for medium-risk fixes.
- Risk: performance on large repos.
  - Mitigation: incremental scanning and parallel execution.

## Definition of Done (MVP)

- Can list issues from source code.
- Can select by rule key and issue ID.
- Can plan and apply safe auto-fixes.
- Produces machine-readable reports for Agent/SKILL.
- Passes unit/integration/idempotence test suite.

## Phase 6 - Production Readiness (Week 7+)

Goals:
- Make execution predictable on real repositories.
- Improve observability and failure diagnostics.
- Prepare for wider team adoption.

Deliverables:
- Structured logging with correlation ID per run.
- Exit code contract for CI usage.
- Performance baseline report (small/medium/large repos).
- Release checklist and versioning policy.

Acceptance criteria:
- CI can fail fast based on deterministic exit codes.
- Users can trace any failed issue through logs.
- Baseline runs are documented and repeatable.

Checks:
- [ ] Logging includes run ID, issue ID, rule key, action, outcome.
- [ ] Exit code matrix documented and enforced in CLI.
- [ ] Benchmark suite added with stable fixture repositories.
- [ ] Release process documented (tag, changelog, artifact).

## Immediate Next Iteration (Suggested Order)

1. Close Phase 1 selector gap: support selection by `issueIds` and add filter tests.
2. Close Phase 2 planner gap: add explicit capability checks in `plan` output.
3. Implement iterative convergence tests for apply loop (`skipped_outdated` + re-detection).
4. Add idempotence tests for all Wave 1 fixers.
5. Add optional verification hooks (`compile`, `test`) behind CLI flags.

## Exit Code Contract (Draft)

- `0`: success, no failures.
- `1`: runtime/internal error.
- `2`: completed with one or more failed fixes.
- `3`: invalid user input or selection.
- `4`: verification hook failed (`compile`/`test`).

## Metrics to Track Per Run

- Detection: scanned files, detected issues, selected issues.
- Apply: attempted, applied, skipped, skipped_outdated, failed.
- Performance: total duration, detection duration, apply duration.
- Stability: iterations until convergence, verification result.
