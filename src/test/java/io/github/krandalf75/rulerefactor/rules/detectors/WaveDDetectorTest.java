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

class WaveDDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsS2111BigDecimalDoubleConstructor() throws IOException {
        Path javaFile = tempDir.resolve("BigDecimalCtor.java");
        Files.writeString(javaFile, """
                import java.math.BigDecimal;

                public class BigDecimalCtor {
                    BigDecimal run() {
                        return new BigDecimal(0.1d);
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S2111"});

        assertTrue(issues.size() >= 1);
        assertTrue(issues.stream().allMatch(i -> i.ruleKey().value().equals("java:S2111")));
    }

    @Test
    void doesNotFlagSafeBigDecimalConstruction() throws IOException {
        Path javaFile = tempDir.resolve("BigDecimalSafe.java");
        Files.writeString(javaFile, """
                import java.math.BigDecimal;

                public class BigDecimalSafe {
                    BigDecimal run() {
                        return BigDecimal.valueOf(0.1d);
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S2111"});

        assertEquals(0, issues.size());
    }
}
