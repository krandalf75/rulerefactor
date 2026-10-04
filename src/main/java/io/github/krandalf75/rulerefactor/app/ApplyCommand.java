package io.github.krandalf75.rulerefactor.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.krandalf75.rulerefactor.core.model.ApplyReport;
import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintStream;
import java.nio.file.Path;

@Command(name = "apply", description = "Apply selected fixes")
public class ApplyCommand implements Runnable {
    @Option(names = "--path", defaultValue = ".", description = "Project path")
    String path;

    @Option(names = "--issue-ids", split = ",", description = "Issue IDs, comma-separated")
    String[] issueIds;

    @Option(names = "--rules", split = ",", description = "Rule keys, comma-separated")
    String[] rules;

    @Option(names = "--dry-run", defaultValue = "false", description = "Preview changes only")
    boolean dryRun;

    @Option(names = "--format", defaultValue = "table", description = "Output format: table|json")
    String format;

    @Option(names = "--progress", defaultValue = "true", description = "Show progress messages")
    boolean progress;

    @Option(names = "--verify", split = ",", description = "Verification hooks after each applied fix: compile,test")
    String[] verify;

    @Override
    public void run() {
        PrintStream out = System.out;
        PrintStream err = System.err;
        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        ApplyReport report = engine.applySelected(
                Path.of(path),
                rules,
                issueIds,
                dryRun,
                verify,
                progress ? message -> err.println("[progress] " + message) : null
        );
        if ("json".equalsIgnoreCase(format)) {
            try {
                out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(report));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot serialize apply report", e);
            }
            return;
        }
        out.printf("detected=%d selected=%d applied=%d skipped=%d skipped_outdated=%d failed=%d%n",
                report.detected(), report.selected(), report.applied(), report.skipped(), report.skippedOutdated(), report.failed());
        for (var entry : report.entries()) {
            out.printf("- %s %s [%s] %s%n", entry.issueRef(), entry.ruleKey(), entry.status(), entry.details());
        }
    }
}
