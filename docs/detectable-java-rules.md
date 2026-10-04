# Detectable Java SonarQube Rules

This document tracks Java SonarQube rules that RuleRefactor can detect.

Status values:
- `implemented`: detection is available now.
- `planned`: detection is planned but not implemented yet.

## Quick view

- Total tracked rules: 50+
- Implemented: 23 (`java:S1128`, `java:S1155`, `java:S2293`, `java:S6204`, `java:S1118`, `java:S1905`, `java:S1481`, `java:S1602`, `java:S106`, `java:S2111`, `java:S1068`, `java:S1148`, `java:S00108`, `java:S1132`, `java:S1612`, `java:S2095`, `java:S2184`, `java:S2325`, `java:S2589`, `java:S3457`, `java:S4348`, `java:S4970`, `java:S5411`)
- In progress: 0
- Planned: 50+

## What matters now

These are the next rules to implement first (low risk, high value):

| Order | Rule Key | Rule name | Autofix | Implemented |
|---:|---|---|---|---|
| 1 | `java:S1155` | Collection emptiness should use `isEmpty()` | yes | yes |
| 2 | `java:S2293` | Diamond operator should be used | yes | yes |
| 3 | `java:S6204` | Prefer `Stream.toList()` when possible | yes | yes |
| 4 | `java:S1905` | Redundant casts should not be used | yes | yes |
| 5 | `java:S1481` | Unused local variables should be removed | yes | yes |
| 6 | `java:S1118` | Utility classes should not have public constructors | yes | yes |
| 7 | `java:S1602` | Single-statement lambdas should not use blocks | yes | yes |
| 8 | `java:S2111` | `BigDecimal(double)` should not be used | yes | yes |

## Implementation status by wave

- Wave A (quick wins): 9/9 completed
- Wave B (medium risk): 13 completed
- Wave C (detect-first): 0 completed

## Implemented

| Rule Key | Sonar Name | Status | Notes |
|---|---|---|---|
| `java:S1128` | Unused imports should be removed | implemented | Backed by OpenRewrite unused-import analysis |
| `java:S1155` | Collection emptiness should use `isEmpty()` | implemented | OpenRewrite AST binary-expression analysis |
| `java:S2293` | Diamond operator should be used | implemented | OpenRewrite AST constructor type analysis |
| `java:S6204` | Prefer `Stream.toList()` when possible | implemented | OpenRewrite AST method invocation analysis |
| `java:S1118` | Utility classes should not have public constructors | implemented | OpenRewrite AST analysis |
| `java:S1905` | Redundant casts should not be used | implemented | OpenRewrite AST analysis |
| `java:S1481` | Unused local variables should be removed | implemented | OpenRewrite AST local-variable usage analysis |
| `java:S1602` | Single-statement lambdas should not use blocks | implemented | OpenRewrite AST lambda body analysis |
| `java:S106` | Standard outputs should not be used directly to log anything | implemented | OpenRewrite AST method invocation analysis |
| `java:S2111` | `BigDecimal(double)` should not be used | implemented | OpenRewrite AST constructor argument type analysis |
| `java:S1068` | Unused private fields should be removed | implemented | OpenRewrite AST class-field usage analysis |
| `java:S1148` | `System.out` and `System.err` should not be used directly | implemented | OpenRewrite AST method invocation analysis |
| `java:S00108` | Nested blocks of code should not be left empty | implemented | OpenRewrite AST empty-block analysis |
| `java:S1132` | Strings literals should be placed on the left side when checking equality | implemented | OpenRewrite recipe `EqualsAvoidsNull` |
| `java:S1612` | Lambdas should be replaced with method references | implemented | OpenRewrite recipe `ReplaceLambdaWithMethodReference` |
| `java:S3457` | `String.contains` should be used instead of `indexOf` checks | implemented | OpenRewrite recipe `IndexOfReplaceableByContains` |
| `java:S4348` | `BigDecimal` equals should not be used for value comparison | implemented | Deterministic regex fixer + detector |
| `java:S4970` | Primitive wrappers should not be instantiated | implemented | OpenRewrite recipe `PrimitiveWrapperClassConstructorToValueOf` |
| `java:S2184` | Math operands should be cast before assignment | implemented | Deterministic regex fixer + detector |
| `java:S2325` | Methods and fields that don't access instance data should be static | implemented | Private-method safe detector + deterministic fixer |
| `java:S2589` | Boolean expressions should not be gratuitous | implemented | OpenRewrite recipe `SimplifyBooleanExpression` |
| `java:S2095` | Resources should be closed | implemented | Deterministic try-with-resources regex fixer + detector |
| `java:S5411` | Boxed booleans should be avoided in boolean expressions | implemented | OpenRewrite recipe `AvoidBoxedBooleanExpressions` |

## Planned (Phase 1/2 backlog)

| Rule Key | Sonar Name | Status | Notes |
|---|---|---|---|
| `java:S1068` | Unused private fields should be removed | planned | Requires careful handling for serialization/reflection |
| `java:S1148` | `System.out` and `System.err` should not be used directly | planned | Detect first, fixer later |
| `java:S00108` | Nested blocks of code should not be left empty | planned | Includes empty catch/body cases |
| `java:S1612` | Lambdas should be replaced with method references | planned | Detection straightforward, fix may be optional |
| `java:S4973` | Strings and boxed types should be compared with `equals()` | planned | Detect only initially; fixer requires semantics checks |
| `java:S1192` | String literals should not be duplicated | planned | Detection only in early phases |
| `java:S2583` | Conditionally executed blocks should be reachable | planned | Detection may need dataflow support |

## Extended Java SonarQube backlog

| Rule Key | Sonar Name | Status | Notes |
|---|---|---|---|
| `java:S1132` | Strings literals should be placed on the left side when checking equality | planned | Optional fixer style rule |
| `java:S1172` | Unused method parameters should be removed | planned | Public API impact, detect first |
| `java:S1186` | Methods should not be empty | planned | Exclude framework-required methods |
| `java:S125` | Sections of code should not be commented out | planned | Detection only |
| `java:S1301` | `switch` statements should have at least 3 `case` clauses | planned | Readability rule |
| `java:S131` | `switch`/`case` clauses should not have too many lines | planned | Readability rule |
| `java:S1448` | `switch` cases should end with an unconditional `break` | planned | Control-flow safety |
| `java:S1450` | Private fields only used as local variables should become locals | planned | Needs lifecycle checks |
| `java:S1596` | Collections should not be synchronized manually | planned | Concurrency rule |
| `java:S1854` | Dead stores should be removed | planned | Dataflow-heavy |
| `java:S1948` | Fields in a `Serializable` class should either be transient or serializable | planned | Semantic checks required |
| `java:S2095` | Resources should be closed | planned | Auto-fix via try-with-resources in some cases |
| `java:S2129` | `PreparedStatement` should be used for SQL queries | planned | Security rule, detect first |
| `java:S2142` | `InterruptedException` should not be ignored | planned | Concurrency correctness |
| `java:S2160` | Subclasses that add fields should override `equals` | planned | Design rule, detect only |
| `java:S2184` | Math operands should be cast before assignment | planned | Numeric precision safety |
| `java:S2201` | Return values from methods should not be ignored | planned | Detect only in first iteration |
| `java:S2221` | Catch blocks should preserve original exceptions | planned | Exception handling quality |
| `java:S2225` | `toString()` and `clone()` methods should not return null | planned | Contract rule |
| `java:S2272` | `Iterator.next()` methods should throw `NoSuchElementException` | planned | API contract |
| `java:S2325` | Methods and fields that don't access instance data should be static | planned | Common refactor |
| `java:S2384` | Mutable members should not be stored or returned directly | planned | Encapsulation rule |
| `java:S2589` | Boolean expressions should not be gratuitous | planned | Simplification candidates |
| `java:S2864` | `Stream.peek` should be used with caution | planned | Code smell detection |
| `java:S2974` | Classes should not be empty | planned | Ignore marker classes by config |
| `java:S3358` | Ternary operators should not be nested | planned | Readability, style |
| `java:S3457` | `String.contains` should be used instead of `indexOf` checks | planned | Low-risk auto-fix |
| `java:S3516` | Methods should not always return the same value | planned | Possible logic bug |
| `java:S3551` | `Optional` should not be used for fields/parameters | planned | Design guidance |
| `java:S3655` | Optional value should only be accessed after checking presence | planned | NPE prevention |
| `java:S3740` | Raw types should not be used | planned | Generics hygiene |
| `java:S3776` | Cognitive complexity of methods should not be too high | planned | Detect/report only |
| `java:S3864` | `Stream.peek` should not be used | planned | Depends on profile version |
| `java:S3981` | Collection size should be checked using `isEmpty()` | planned | Similar to `S1155` |
| `java:S4034` | Classes implementing `Comparable` should override `equals` | planned | Contract rule |
| `java:S4138` | `Iterator.hasNext()` should be called before `next()` | planned | Correctness rule |
| `java:S4144` | Methods should not have identical implementations | planned | Clone detection |
| `java:S4274` | Runtime exceptions should not be caught | planned | Error-handling quality |
| `java:S4348` | `BigDecimal` equals should not be used for value comparison | planned | Use compareTo |
| `java:S4970` | Primitive wrappers should not be instantiated | planned | Prefer valueOf/autoboxing |
| `java:S5411` | Boxed booleans should be avoided in boolean expressions | planned | NPE prevention |
| `java:S5778` | Only one method invocation is expected when testing runtime exceptions | planned | Unit test quality |
| `java:S5783` | Only one method invocation is expected when testing checked exceptions | planned | Unit test quality |

## Candidate count

- Implemented: 1
- Implemented: 23
- Planned backlog (core + extended): 50+

## Prioritized Top 30 (Complexity + Security)

Legend:
- `Autofix`: `yes`, `partial`, `no`
- `Regression risk`: `low`, `medium`, `high`
- `Implemented`: `yes`, `in progress`, `no`

| Priority | Rule Key | Category | Rule name | Autofix | Regression risk | Implemented |
|---:|---|---|---|---|---|
| 1 | `java:S1128` | Maintainability | Unused imports should be removed | yes | low | yes |
| 2 | `java:S1155` | Maintainability | Collection emptiness should use `isEmpty()` | yes | low | yes |
| 3 | `java:S2293` | Maintainability | Diamond operator should be used | yes | low | yes |
| 4 | `java:S6204` | Maintainability | Prefer `Stream.toList()` when possible | yes | low | yes |
| 5 | `java:S1905` | Maintainability | Redundant casts should not be used | yes | low | yes |
| 6 | `java:S1481` | Maintainability | Unused local variables should be removed | yes | low | yes |
| 7 | `java:S1068` | Maintainability | Unused private fields should be removed | partial | medium | no |
| 8 | `java:S106` | Maintainability | Standard outputs should not be used to log | yes | medium | yes |
| 9 | `java:S1148` | Maintainability | `System.out/err` should not be used directly | partial | medium | no |
| 10 | `java:S00108` | Reliability | Nested blocks should not be empty | partial | medium | no |
| 11 | `java:S1118` | Maintainability | Utility classes should not have public constructors | yes | low | yes |
| 12 | `java:S2325` | Maintainability | Members not using instance data should be static | partial | medium | no |
| 13 | `java:S1602` | Maintainability | Single-statement lambdas should not use blocks | yes | low | yes |
| 14 | `java:S1612` | Maintainability | Lambdas should be replaced with method references | partial | medium | no |
| 15 | `java:S2095` | Reliability | Resources should be closed | partial | medium | no |
| 16 | `java:S2111` | Reliability | `BigDecimal(double)` should not be used | yes | low | yes |
| 17 | `java:S4348` | Reliability | `BigDecimal.equals()` should not compare values | partial | medium | no |
| 18 | `java:S3655` | Reliability | Optional should be checked before `get()` | no | medium | no |
| 19 | `java:S5411` | Reliability | Boxed booleans in expressions should be avoided | partial | medium | no |
| 20 | `java:S2221` | Reliability | Catch blocks should preserve original exceptions | no | medium | no |
| 21 | `java:S2142` | Reliability | `InterruptedException` should not be ignored | no | high | no |
| 22 | `java:S2184` | Reliability | Math operands should be cast before assignment | partial | medium | no |
| 23 | `java:S2201` | Reliability | Return values from methods should not be ignored | no | medium | no |
| 24 | `java:S2583` | Reliability | Conditionally executed blocks should be reachable | no | high | no |
| 25 | `java:S2589` | Maintainability | Boolean expressions should not be gratuitous | partial | medium | no |
| 26 | `java:S3776` | Complexity | Cognitive complexity should not be too high | no | high | no |
| 27 | `java:S3516` | Reliability | Methods should not always return the same value | no | high | no |
| 28 | `java:S2129` | Security | SQL queries should use prepared statements | partial | high | no |
| 29 | `java:S1948` | Security | Fields in `Serializable` classes should be safe | no | high | no |
| 30 | `java:S4274` | Security/Reliability | Runtime exceptions should not be caught | no | high | no |

### Notes for implementation waves

- **Wave A (quick wins, low risk)**: priorities 1-6, 11, 13, 16.
- **Wave B (medium risk, guarded autofix)**: priorities 7-10, 12, 14-15, 17, 19, 22, 25.
- **Wave C (detect-first, manual review)**: priorities 18, 20-21, 23-24, 26-30.

## Scope note

- RuleRefactor currently focuses on **Java source-level detectable issues**.
- Some Sonar rules require deep semantic/dataflow/project context and may be added later.
