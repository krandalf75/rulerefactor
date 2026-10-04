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

class WaveCDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsS1602SingleStatementLambdaBlock() throws IOException {
        Path javaFile = tempDir.resolve("LambdaSample.java");
        Files.writeString(javaFile, """
                import java.util.List;

                public class LambdaSample {
                    void run(List<String> values) {
                        values.forEach(v -> { System.out.println(v); });
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1602"});

        assertEquals(1, issues.size());
        assertEquals("java:S1602", issues.getFirst().ruleKey().value());
    }

    @Test
    void detectsS106StandardOutputUsage() throws IOException {
        Path javaFile = tempDir.resolve("StdOutSample.java");
        Files.writeString(javaFile, """
                public class StdOutSample {
                    void run() {
                        System.out.println("hello");
                        System.err.print("error");
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S106"});

        assertEquals(2, issues.size());
        assertTrue(issues.stream().allMatch(i -> i.ruleKey().value().equals("java:S106")));
    }
}
