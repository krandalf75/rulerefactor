package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MathOperandsCastDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S2184");
    private static final Pattern PATTERN = Pattern.compile("(long|float|double)\\s+[A-Za-z_][A-Za-z0-9_]*\\s*=\\s*[A-Za-z_][A-Za-z0-9_]*\\s*[/+*-]\\s*[A-Za-z_][A-Za-z0-9_]*\\s*;");

    @Override
    public RuleKey ruleKey() {
        return RULE_KEY;
    }

    @Override
    public List<CodeIssue> detect(Path projectPath) {
        List<CodeIssue> issues = new ArrayList<>();
        AtomicInteger seq = new AtomicInteger(1);
        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                String source;
                try {
                    source = Files.readString(path);
                } catch (IOException e) {
                    return;
                }
                if (PATTERN.matcher(source).find()) {
                    issues.add(new CodeIssue(
                            "ISSUE-" + seq.getAndIncrement(),
                            RULE_KEY,
                            projectPath.relativize(path).toString(),
                            -1,
                            "Cast one operand before arithmetic operation."
                    ));
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan project path: " + projectPath, e);
        }
        return issues;
    }
}
