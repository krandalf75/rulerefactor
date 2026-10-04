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

class WaveARulesDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsS1155S2293S6204() throws IOException {
        Path javaFile = tempDir.resolve("WaveA.java");
        Files.writeString(javaFile, """
                import java.util.List;
                import java.util.ArrayList;
                import java.util.stream.Collectors;

                public class WaveA {
                    void run(List<String> in) {
                        if (in.size() == 0) {
                            return;
                        }
                        List<String> a = new ArrayList<String>();
                        List<String> b = in.stream().map(String::trim).collect(Collectors.toList());
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> s1155 = engine.listIssues(tempDir, new String[]{"java:S1155"});
        List<CodeIssue> s2293 = engine.listIssues(tempDir, new String[]{"java:S2293"});
        List<CodeIssue> s6204 = engine.listIssues(tempDir, new String[]{"java:S6204"});

        assertTrue(s1155.size() >= 1);
        assertTrue(s2293.size() >= 1);
        assertTrue(s6204.size() >= 1);
    }

    @Test
    void filtersByNewRules() throws IOException {
        Path javaFile = tempDir.resolve("Filter.java");
        Files.writeString(javaFile, """
                import java.util.List;
                import java.util.ArrayList;

                public class Filter {
                    void run(List<String> in) {
                        if (in.size() == 0) {
                            return;
                        }
                        List<String> a = new ArrayList<String>();
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> s1155 = engine.listIssues(tempDir, new String[]{"java:S1155"});
        List<CodeIssue> s2293 = engine.listIssues(tempDir, new String[]{"java:S2293"});

        assertTrue(s1155.size() >= 1);
        assertEquals("java:S1155", s1155.getFirst().ruleKey().value());
        assertTrue(s2293.size() >= 1);
        assertEquals("java:S2293", s2293.getFirst().ruleKey().value());
    }
}
