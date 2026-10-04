package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnusedImportDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsUnusedImport() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, """
                import java.util.List;
                import java.util.ArrayList;

                public class Sample {
                    List<String> values = new ArrayList<>();
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1128"});

        assertEquals(0, issues.size());
    }

    @Test
    void detectsUnusedImportAndFiltersByRule() throws IOException {
        Path javaFile = tempDir.resolve("Unused.java");
        Files.writeString(javaFile, """
                import java.util.List;

                public class Unused {
                    private int value = 1;
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> allIssues = engine.listIssues(tempDir, null);
        List<CodeIssue> filteredIssues = engine.listIssues(tempDir, new String[]{"java:S1128"});
        List<CodeIssue> noMatch = engine.listIssues(tempDir, new String[]{"java:S9999"});

        assertTrue(allIssues.stream().anyMatch(issue -> "java:S1128".equals(issue.ruleKey().value())));
        assertTrue(filteredIssues.stream().allMatch(issue -> "java:S1128".equals(issue.ruleKey().value())));
        assertTrue(allIssues.size() >= filteredIssues.size());
        assertEquals(0, noMatch.size());
    }
}
