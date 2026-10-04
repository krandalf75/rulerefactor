package io.github.krandalf75.rulerefactor.app;

import picocli.CommandLine;

public final class RuleRefactorApplication {
    private static final int REQUIRED_JAVA_FEATURE = 21;

    private RuleRefactorApplication() {
    }

    public static void main(String[] args) {
        int currentFeature = Runtime.version().feature();
        if (currentFeature != REQUIRED_JAVA_FEATURE) {
            System.exit(2);
        }
        int exitCode = new CommandLine(new RuleRefactorCommand()).execute(args);
        System.exit(exitCode);
    }
}
