package io.github.krandalf75.rulerefactor.core.service;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssueSelectorTest {

    @Test
    void filtersByIssueIds() {
        CodeIssue s1155 = new CodeIssue("a", new RuleKey("java:S1155"), "A.java", 10, "m1");
        CodeIssue s2293 = new CodeIssue("b", new RuleKey("java:S2293"), "B.java", 20, "m2");

        List<CodeIssue> filtered = IssueSelector.filterByIssueIds(List.of(s1155, s2293), new String[]{"b"});

        assertEquals(1, filtered.size());
        assertEquals("b", filtered.getFirst().issueId());
    }

    @Test
    void filtersByRulesAndIssueIdsTogether() {
        CodeIssue s1155a = new CodeIssue("a", new RuleKey("java:S1155"), "A.java", 10, "m1");
        CodeIssue s1155b = new CodeIssue("b", new RuleKey("java:S1155"), "A.java", 12, "m2");
        CodeIssue s2293 = new CodeIssue("c", new RuleKey("java:S2293"), "B.java", 20, "m3");

        List<CodeIssue> filtered = IssueSelector.filterBySelection(
                List.of(s1155a, s1155b, s2293),
                new String[]{"java:S1155"},
                new String[]{"b", "c"}
        );

        assertEquals(1, filtered.size());
        assertEquals("b", filtered.getFirst().issueId());
    }
}
