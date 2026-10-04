package io.github.krandalf75.rulerefactor.core.service;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefactorEngineRulesFilterTest {

    @TempDir
    Path tempDir;

    @Test
    void listIssuesRespectsRulesFilter() throws IOException {
        Path javaFile = tempDir.resolve("FilterRules.java");
        Files.writeString(javaFile, """
                import java.util.ArrayList;
                import java.util.List;

                class FilterRules {
                    void run(List<String> in) {
                        if (in.size() == 0) {
                            return;
                        }
                        List<String> list = new ArrayList<String>();
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        List<CodeIssue> issues = engine.listIssues(tempDir, new String[]{"java:S1155"});

        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().allMatch(i -> "java:S1155".equals(i.ruleKey().value())));
    }
}
