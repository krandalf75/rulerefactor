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

class Wave1IdempotenceTest {

    @TempDir
    Path tempDir;

    @Test
    void s1128IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S1128", """
                import java.util.List;
                import java.util.ArrayList;
                import java.util.HashMap;

                class Sample {
                    void run() {
                        List<String> list = new ArrayList<>();
                    }
                }
                """);
    }

    @Test
    void s1155IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S1155", """
                import java.util.List;

                class Sample {
                    void run(List<String> in) {
                        if (in.size() == 0) {
                            return;
                        }
                    }
                }
                """);
    }

    @Test
    void s2293IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S2293", """
                import java.util.ArrayList;
                import java.util.List;

                class Sample {
                    void run() {
                        List<String> list = new ArrayList<String>();
                    }
                }
                """);
    }

    @Test
    void s6204IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S6204", """
                import java.util.List;
                import java.util.stream.Collectors;

                class Sample {
                    void run(List<String> in) {
                        List<String> out = in.stream().map(String::trim).collect(Collectors.toList());
                    }
                }
                """);
    }

    @Test
    void s1905IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S1905", """
                class Sample {
                    void run() {
                        String value = (String) "ok";
                    }
                }
                """);
    }

    @Test
    void s1481IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S1481", """
                class Sample {
                    void run() {
                        int unused = 42;
                    }
                }
                """);
    }

    @Test
    void s1118IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S1118", """
                class Utility {
                    public Utility() {
                    }

                    static String trim(String in) {
                        return in.trim();
                    }
                }
                """);
    }

    @Test
    void s1602IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S1602", """
                import java.util.function.Function;

                class Sample {
                    void run() {
                        Function<String, String> fn = s -> {
                            return s.trim();
                        };
                    }
                }
                """);
    }

    @Test
    void s2111IsIdempotent() throws IOException {
        assertRuleIsIdempotent("java:S2111", """
                import java.math.BigDecimal;

                class Sample {
                    void run() {
                        BigDecimal value = new BigDecimal(0.1);
                    }
                }
                """);
    }

    private void assertRuleIsIdempotent(String ruleKey, String source) throws IOException {
        Path ruleDir = Files.createDirectories(tempDir.resolve(ruleKey.replace(':', '_')));
        Path javaFile = ruleDir.resolve("Sample.java");
        Files.writeString(javaFile, source);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());

        ApplyReport first = engine.applySelected(ruleDir, new String[]{ruleKey}, null, false);
        ApplyReport second = engine.applySelected(ruleDir, new String[]{ruleKey}, null, false);

        assertTrue(first.applied() >= 1, "Expected first apply to change code for " + ruleKey);
        assertEquals(0, second.applied(), "Expected second apply to be idempotent for " + ruleKey);
    }
}
