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

class Wave2DetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsS1068UnusedPrivateField() throws IOException {
        Path javaFile = tempDir.resolve("UnusedPrivateField.java");
        Files.writeString(javaFile, """
                class UnusedPrivateField {
                    private String unused = "x";
                    private String used = "y";

                    String run() {
                        return used;
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1068"});

        assertEquals(1, issues.size());
        assertEquals("java:S1068", issues.getFirst().ruleKey().value());
    }

    @Test
    void detectsS1148SystemOutErrUsage() throws IOException {
        Path javaFile = tempDir.resolve("SystemOutErr.java");
        Files.writeString(javaFile, """
                class SystemOutErr {
                    void run() {
                        System.out.println("hello");
                        System.err.println("boom");
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1148"});

        assertEquals(2, issues.size());
        assertTrue(issues.stream().allMatch(i -> "java:S1148".equals(i.ruleKey().value())));
    }

    @Test
    void detectsS00108EmptyNestedBlocks() throws IOException {
        Path javaFile = tempDir.resolve("EmptyBlocks.java");
        Files.writeString(javaFile, """
                class EmptyBlocks {
                    void run(boolean flag) {
                        if (flag) {
                        }
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S00108"});

        assertEquals(1, issues.size());
        assertEquals("java:S00108", issues.getFirst().ruleKey().value());
    }
}
