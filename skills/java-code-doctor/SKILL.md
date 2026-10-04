---
name: java-code-doctor
description: Detect Java code quality and correctness issues with RuleRefactor, review safe fixes, apply selected changes, and verify them. Use when asked to inspect or repair Java code in any repository.
---

# Java Code Doctor

Use the standalone RuleRefactor CLI to scan Java source, preview its available fixes, and apply only suitable changes. It can be used against any Java repository; the target repository does not need RuleRefactor as a dependency.

## Find the RuleRefactor JAR

Use the first valid executable JAR found in this order:

1. The path in `RULE_REFACTOR_JAR`.
2. `~/.cache/rulerefactor/rulerefactor.jar`.
3. A built `target/rulerefactor-*.jar` in the RuleRefactor source checkout, if that checkout is available.
4. If no local JAR is available, download the latest standalone JAR from `https://github.com/krandalf75/rulerefactor/releases/latest/download/rulerefactor.jar` and save it as `~/.cache/rulerefactor/rulerefactor.jar`. Request network access if the environment requires approval.

Confirm the file exists and run it with Java 21 (`java -version`, then `java -jar "$JAR" --help`). Do not mistake a target project's own JAR for RuleRefactor. Do not replace a user-configured `RULE_REFACTOR_JAR` with the downloaded default.

## Scan and fix

1. Identify the target repository root and inspect its instructions and working-tree changes. Keep existing user edits intact.
2. Run `java -jar "$JAR" list-issues --path <project-root> --format json` to discover current findings. The tool currently detects a defined set of Java rules; it is not a general compiler, security scanner, or complete correctness proof.
3. Run `java -jar "$JAR" plan --path <project-root>` to see which findings have fixes and their confidence. Explain notable findings briefly. Do not treat detected issues as proof of a defect without checking their source context.
4. Apply only findings whose proposed edits are clear and appropriate. Prefer selecting reviewed `issueIds`:

   ```sh
   java -jar "$JAR" apply --path <project-root> --issue-ids ISSUE-1001,ISSUE-1002 --format json
   ```

   Do not apply every rule indiscriminately when the plan includes manual-only, uncertain, or behavior-sensitive findings. For those, explain the issue and make a considered source edit if the requested scope supports it.
5. Inspect the diff after edits. Run the target project's relevant compile or tests when available and proportionate; do not alter build configuration just to make verification run. Report what changed, what was verified, and any findings left for manual review.

The CLI supports `list-issues`, `plan`, and `apply`. `apply` also accepts `--rules`, `--dry-run`, and `--verify compile,test`; check the installed CLI's `--help` before relying on version-specific options. RuleRefactor currently requires Java 21 and its coverage is limited to implemented rules.
