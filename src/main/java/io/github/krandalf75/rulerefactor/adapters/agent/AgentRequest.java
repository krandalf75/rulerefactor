package io.github.krandalf75.rulerefactor.adapters.agent;

public record AgentRequest(
        String mode,
        String projectPath,
        Selection selection,
        boolean dryRun,
        String[] verify
) {
    public record Selection(String[] ruleKeys, String[] issueIds) {
    }
}
