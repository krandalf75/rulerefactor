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

class WaveBDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsS1481UnusedLocalVariable() throws IOException {
        Path javaFile = tempDir.resolve("UnusedLocal.java");
        Files.writeString(javaFile, """
                public class UnusedLocal {
                    int run() {
                        int used = 1;
                        int unused = 2;
                        return used;
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1481"});

        assertEquals(1, issues.size());
        assertEquals("java:S1481", issues.getFirst().ruleKey().value());
    }

    @Test
    void filtersS1481WithoutNoise() throws IOException {
        Path javaFile = tempDir.resolve("UnusedLocalFilter.java");
        Files.writeString(javaFile, """
                public class UnusedLocalFilter {
                    int run() {
                        int local = 5;
                        return local;
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1481"});

        assertTrue(issues.isEmpty());
    }
}
