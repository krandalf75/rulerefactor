package io.github.krandalf75.rulerefactor.core.model;

import java.util.List;

public record ApplyReport(
        int detected,
        int selected,
        int applied,
        int skipped,
        int skippedOutdated,
        int failed,
        List<ApplyEntry> entries
) {
}
