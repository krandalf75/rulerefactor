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

class WaveASecondBatchDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsS1118AndS1905() throws IOException {
        Path javaFile = tempDir.resolve("Batch2.java");
        Files.writeString(javaFile, """
                public class Batch2 {
                    public static final String A = "x";

                    public Batch2() {
                    }

                    public static int val() {
                        int x = 1;
                        return (int) x;
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, null);

        assertTrue(issues.stream().anyMatch(i -> i.ruleKey().value().equals("java:S1118")));
        assertTrue(issues.stream().anyMatch(i -> i.ruleKey().value().equals("java:S1905")));
    }

    @Test
    void filtersNewRules() throws IOException {
        Path javaFile = tempDir.resolve("Batch2Filter.java");
        Files.writeString(javaFile, """
                public class Batch2Filter {
                    public static final String A = "x";

                    public Batch2Filter() {
                    }

                    public static int val() {
                        int x = 1;
                        return (int) x;
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> s1118 = engine.listIssues(tempDir, new String[]{"java:S1118"});
        List<CodeIssue> s1905 = engine.listIssues(tempDir, new String[]{"java:S1905"});

        assertEquals(1, s1118.size());
        assertEquals("java:S1118", s1118.getFirst().ruleKey().value());
        assertEquals(1, s1905.size());
        assertEquals("java:S1905", s1905.getFirst().ruleKey().value());
    }
}
