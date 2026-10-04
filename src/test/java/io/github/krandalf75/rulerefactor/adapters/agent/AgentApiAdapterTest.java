package io.github.krandalf75.rulerefactor.adapters.agent;

import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;
import io.github.krandalf75.rulerefactor.registry.InMemoryRuleRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentApiAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void supportsListIssuesPlanAndApplyModes() throws IOException {
        Files.writeString(tempDir.resolve("Sample.java"), """
                import java.util.ArrayList;
                import java.util.List;

                class Sample {
                    void run(List<String> in) {
                        if (in.size() == 0) {
                            return;
                        }
                        List<String> a = new ArrayList<String>();
                    }
                }
                """);

        AgentApiAdapter adapter = new AgentApiAdapter(new RefactorEngine(new InMemoryRuleRegistry()));

        AgentRequest listReq = new AgentRequest("list-issues", tempDir.toString(), new AgentRequest.Selection(new String[]{"java:S1155"}, null), true, null);
        AgentResponse listRes = adapter.execute(listReq);
        assertEquals("list-issues", listRes.mode());
        assertTrue(listRes.detected() >= 1);
        assertNull(listRes.error());

        AgentRequest planReq = new AgentRequest("plan", tempDir.toString(), new AgentRequest.Selection(new String[]{"java:S2293"}, null), true, null);
        AgentResponse planRes = adapter.execute(planReq);
        assertEquals("plan", planRes.mode());
        assertTrue(planRes.selected() >= 1);

        AgentRequest applyReq = new AgentRequest("apply", tempDir.toString(), new AgentRequest.Selection(new String[]{"java:S2293"}, null), false, null);
        AgentResponse applyRes = adapter.execute(applyReq);
        assertEquals("apply", applyRes.mode());
        assertTrue(applyRes.applied() >= 1);
        assertEquals(0, applyRes.failed());
    }
}
