package io.github.krandalf75.rulerefactor.app;

import io.github.krandalf75.rulerefactor.adapters.cli.IssueOutputFormatter;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.io.PrintStream;
import java.util.List;

@Command(name = "list-issues", description = "Detect and list issues")
public class ListIssuesCommand implements Runnable {
    @Option(names = "--path", defaultValue = ".", description = "Project path")
    String path;

    @Option(names = "--rules", split = ",", description = "Rule keys, comma-separated")
    String[] rules;

    @Option(names = "--format", defaultValue = "table", description = "Output format: table|json")
    String format;

    @Override
    public void run() {
        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(Path.of(path), rules);
        PrintStream out = System.out;
        out.print(IssueOutputFormatter.format(issues, format));
    }
}
