package io.github.krandalf75.rulerefactor.core.service;

import io.github.krandalf75.rulerefactor.core.model.ApplyReport;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Wave2FixableRulesTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsAndFixesS1132() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S1132", """
                class Sample {
                    boolean run(String name) {
                        return name.equals("x");
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS3457() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S3457", """
                class Sample {
                    boolean run(String name) {
                        return name.indexOf("x") >= 0;
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS4970() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S4970", """
                class Sample {
                    Integer run() {
                        return new Integer(7);
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS1612() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S1612", """
                import java.util.function.UnaryOperator;

                class Sample {
                    UnaryOperator<String> run() {
                        return value -> value.trim();
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS4348() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S4348", """
                import java.math.BigDecimal;

                class Sample {
                    boolean run(BigDecimal a, BigDecimal b) {
                        return a.equals(b);
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS2325() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S2325", """
                class Sample {
                    private int sum(int a, int b) {
                        return a + b;
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS2184() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S2184", """
                class Sample {
                    long run(int a, int b) {
                        long total = a / b;
                        return total;
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS2589() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S2589", """
                class Sample {
                    boolean run(boolean enabled) {
                        if (enabled == true) {
                            return true;
                        }
                        return false;
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS5411() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S5411", """
                class Sample {
                    boolean run(Boolean enabled) {
                        if (enabled) {
                            return true;
                        }
                        return false;
                    }
                }
                """);
    }

    @Test
    void detectsAndFixesS2095() throws IOException {
        assertRuleDetectsAndIsIdempotent("java:S2095", """
                import java.io.ByteArrayInputStream;
                import java.io.InputStream;

                class Sample {
                    int run(byte[] data) throws Exception {
                        InputStream in = new ByteArrayInputStream(data);
                        try {
                            return in.read();
                        } finally {
                            in.close();
                        }
                    }
                }
                """);
    }

    private void assertRuleDetectsAndIsIdempotent(String ruleKey, String source) throws IOException {
        Path ruleDir = Files.createDirectories(tempDir.resolve(ruleKey.replace(':', '_')));
        Path javaFile = ruleDir.resolve("Sample.java");
        Files.writeString(javaFile, source);

        RefactorEngine engine = new RefactorEngine(new InMemoryRuleRegistry());

        List<CodeIssue> detected = engine.listIssues(ruleDir, new String[]{ruleKey});
        assertFalse(detected.isEmpty(), "Expected detection for " + ruleKey);
        assertTrue(detected.stream().allMatch(issue -> ruleKey.equals(issue.ruleKey().value())));

        ApplyReport first = engine.applySelected(ruleDir, new String[]{ruleKey}, null, false);
        ApplyReport second = engine.applySelected(ruleDir, new String[]{ruleKey}, null, false);

        assertTrue(first.applied() >= 1, "Expected first apply to change code for " + ruleKey);
        assertEquals(0, second.applied(), "Expected second apply to be idempotent for " + ruleKey);
    }
}
