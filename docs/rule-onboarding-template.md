# Rule Onboarding Template

Use this checklist when adding a new rule.

## 1) Rule Metadata

- Rule key: `java:SXXXX`
- Title:
- Category:
- Detectable: `true|false`
- Fixable: `true|false`
- Confidence: `high|medium|low`

## 2) Detector

- Add detector class under `rules.detectors`.
- Ensure deterministic issue messages and stable behavior.
- Register detector in `InMemoryRuleRegistry`.

## 3) Fixer (if safe)

- Add fixer class under `rules.fixers`.
- Ensure transformation is deterministic and idempotent.
- Register fixer in `InMemoryRuleRegistry`.

## 4) Capability Registration

- Add `RuleCapability` entry with valid confidence.
- Ensure `detectable=true` has detector registered.
- Ensure `fixable=true` has fixer registered.

## 5) Tests

- Detection test: positive and negative fixture.
- Apply test: change is applied when expected.
- Idempotence test: second run applies zero changes.

## 6) Docs

- Update `docs/detectable-java-rules.md`.
- Update `docs/implementation-plan.md` if milestone changed.
- Add payload example if relevant for `agent-api`.
