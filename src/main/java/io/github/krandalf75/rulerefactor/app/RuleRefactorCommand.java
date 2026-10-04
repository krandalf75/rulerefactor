package io.github.krandalf75.rulerefactor.app;

import picocli.CommandLine.Command;

@Command(
        name = "rulerefactor",
        mixinStandardHelpOptions = true,
        version = "RuleRefactor 0.1.0",
        description = "Automated rule-based Java refactoring",
        subcommands = {
                ListIssuesCommand.class,
                PlanCommand.class,
                ApplyCommand.class,
                AgentApiCommand.class
        }
)
public class RuleRefactorCommand implements Runnable {
    @Override
    public void run() {
    }
}
