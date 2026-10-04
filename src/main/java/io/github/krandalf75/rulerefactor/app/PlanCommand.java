package io.github.krandalf75.rulerefactor.app;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

@Command(name = "plan", description = "Preview proposed fixes")
public class PlanCommand implements Runnable {
    @Option(names = "--path", defaultValue = ".", description = "Project path")
    String path;

    @Option(names = "--rules", split = ",", description = "Rule keys, comma-separated")
    String[] rules;

    @Override
    public void run() {
        InMemoryRuleRegistry registry = new InMemoryRuleRegistry();
        RefactorEngine engine = new RefactorEngine(registry);
        List<CodeIssue> issues = engine.listIssues(Path.of(path), rules);
        PrintStream out = System.out;
        out.printf("Plan candidates: %d issues%n", issues.size());
        for (CodeIssue issue : issues) {
            var capability = registry.capabilities().stream()
                    .filter(c -> c.ruleKey().value().equals(issue.ruleKey().value()))
                    .findFirst()
                    .orElse(null);

            boolean fixable = capability != null && capability.fixable();
            String confidence = capability != null ? capability.confidence() : "unknown";
            String action = fixable ? "apply_available" : "manual_only";

            out.printf("- %s %s [%s|confidence=%s] %s :: %s%n",
                    issue.issueId(),
                    issue.ruleKey().value(),
                    action,
                    confidence,
                    issue.message(),
                    issue.filePath());
        }
    }
}
