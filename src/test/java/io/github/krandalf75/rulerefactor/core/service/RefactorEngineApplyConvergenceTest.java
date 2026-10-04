package io.github.krandalf75.rulerefactor.core.service;

import io.github.krandalf75.rulerefactor.core.model.ApplyReport;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefactorEngineApplyConvergenceTest {

    @TempDir
    Path tempDir;

    @Test
    void secondApplyRunDoesNotCreateExtraChanges() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, """
                import java.util.ArrayList;
                import java.util.List;

                class Sample {
                    void run() {
                        List<String> list = new ArrayList<String>();
                    }
                }
                """);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());

        ApplyReport first = engine.applySelected(tempDir, new String[]{"java:S2293"}, null, false);
        ApplyReport second = engine.applySelected(tempDir, new String[]{"java:S2293"}, null, false);

        assertTrue(first.applied() >= 1);
        assertEquals(0, second.applied());
    }

    @Test
    void rollsBackWhenVerificationHookFails() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        String before = """
                import java.util.ArrayList;
                import java.util.List;

                class Sample {
                    void run() {
                        List<String> list = new ArrayList<String>();
                    }
                }
                """;
        Files.writeString(javaFile, before);

        Path mvnw = tempDir.resolve("mvnw");
        Files.writeString(mvnw, "#!/bin/zsh\nexit 1\n");
        mvnw.toFile().setExecutable(true);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());
        ApplyReport report = engine.applySelected(tempDir, new String[]{"java:S2293"}, null, false, new String[]{"compile"}, null);

        assertEquals(0, report.applied());
        assertEquals(1, report.failed());
        assertEquals(before, Files.readString(javaFile));
    }
}
