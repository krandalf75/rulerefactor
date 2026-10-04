package io.github.krandalf75.rulerefactor.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.krandalf75.rulerefactor.adapters.agent.AgentApiAdapter;
import io.github.krandalf75.rulerefactor.adapters.agent.AgentRequest;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Command(name = "agent-api", description = "Run non-interactive JSON agent payload")
public class AgentApiCommand implements Runnable {
    @Option(names = "--input", description = "Path to JSON request file")
    String input;

    @Override
    public void run() {
        PrintStream out = System.out;
        ObjectMapper mapper = new ObjectMapper();
        try {
            String payload = input == null || input.isBlank()
                    ? new String(System.in.readAllBytes())
                    : Files.readString(Path.of(input));
            AgentRequest request = mapper.readValue(payload, AgentRequest.class);
            AgentApiAdapter adapter = new AgentApiAdapter(new RefactorEngine(new InMemoryRuleRegistry()));
            var response = adapter.execute(request);
            out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot execute agent-api command", e);
        }
    }
}
