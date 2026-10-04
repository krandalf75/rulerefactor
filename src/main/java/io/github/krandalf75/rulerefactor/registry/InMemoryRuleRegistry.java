package io.github.krandalf75.rulerefactor.registry;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.api.IssueFixer;
import io.github.krandalf75.rulerefactor.core.api.RuleCapability;
import io.github.krandalf75.rulerefactor.core.api.RuleRegistry;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import io.github.krandalf75.rulerefactor.rules.detectors.CollectionIsEmptyDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.BigDecimalDoubleConstructorDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.BigDecimalEqualsDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.DiamondOperatorDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.EmptyNestedBlockDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.MathOperandsCastDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.MethodCouldBeStaticDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.OpenRewriteRecipeDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.RedundantCastDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.ResourcesShouldBeClosedDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.SingleStatementLambdaDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.StandardOutputUsageDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.StreamToListDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.SystemOutErrUsageDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.UtilityClassConstructorDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.UnusedImportDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.UnusedLocalVariableDetector;
import io.github.krandalf75.rulerefactor.rules.detectors.UnusedPrivateFieldDetector;
import io.github.krandalf75.rulerefactor.rules.fixers.OpenRewriteRecipeFixer;
import io.github.krandalf75.rulerefactor.rules.fixers.TextRegexIssueFixer;
import io.github.krandalf75.rulerefactor.rules.fixers.TryWithResourcesRegexFixer;
import io.github.krandalf75.rulerefactor.rules.fixers.UnusedImportFixer;
import io.github.krandalf75.rulerefactor.rules.fixers.recipes.StreamCollectToListRecipe;
import io.github.krandalf75.rulerefactor.rules.fixers.recipes.UseDiamondOperatorSimpleRecipe;
import io.github.krandalf75.rulerefactor.rules.fixers.recipes.RemoveStandardOutputCallsRecipe;
import org.openrewrite.staticanalysis.BigDecimalDoubleConstructorRecipe;
import org.openrewrite.staticanalysis.HideUtilityClassConstructor;
import org.openrewrite.staticanalysis.IsEmptyCallOnCollections;
import org.openrewrite.staticanalysis.LambdaBlockToExpression;
import org.openrewrite.staticanalysis.EqualsAvoidsNull;
import org.openrewrite.staticanalysis.IndexOfReplaceableByContains;
import org.openrewrite.staticanalysis.PrimitiveWrapperClassConstructorToValueOf;
import org.openrewrite.staticanalysis.RemoveRedundantTypeCast;
import org.openrewrite.staticanalysis.ReplaceLambdaWithMethodReference;
import org.openrewrite.staticanalysis.RemoveUnusedLocalVariables;
import org.openrewrite.staticanalysis.SimplifyBooleanExpression;
import org.openrewrite.staticanalysis.AvoidBoxedBooleanExpressions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;

public class InMemoryRuleRegistry implements RuleRegistry {
    private final Map<String, IssueDetector> detectors;
    private final Map<String, IssueFixer> fixers;
    private final List<RuleCapability> capabilities;

    public InMemoryRuleRegistry() {
        IssueDetector unusedImportDetector = new UnusedImportDetector();
        IssueDetector collectionIsEmptyDetector = new CollectionIsEmptyDetector();
        IssueDetector bigDecimalDoubleConstructorDetector = new BigDecimalDoubleConstructorDetector();
        IssueDetector diamondOperatorDetector = new DiamondOperatorDetector();
        IssueDetector streamToListDetector = new StreamToListDetector();
        IssueDetector utilityClassConstructorDetector = new UtilityClassConstructorDetector();
        IssueDetector redundantCastDetector = new RedundantCastDetector();
        IssueDetector unusedLocalVariableDetector = new UnusedLocalVariableDetector();
        IssueDetector singleStatementLambdaDetector = new SingleStatementLambdaDetector();
        IssueDetector standardOutputUsageDetector = new StandardOutputUsageDetector();
        IssueDetector unusedPrivateFieldDetector = new UnusedPrivateFieldDetector();
        IssueDetector systemOutErrUsageDetector = new SystemOutErrUsageDetector();
        IssueDetector emptyNestedBlockDetector = new EmptyNestedBlockDetector();
        IssueDetector literalsOnLeftEqualsDetector = new OpenRewriteRecipeDetector(
                "java:S1132",
                "Move string literal to the left side of equals comparison.",
                EqualsAvoidsNull::new
        );
        IssueDetector indexOfContainsDetector = new OpenRewriteRecipeDetector(
                "java:S3457",
                "Use contains() instead of indexOf(...) comparison.",
                IndexOfReplaceableByContains::new
        );
        IssueDetector wrapperCtorDetector = new OpenRewriteRecipeDetector(
                "java:S4970",
                "Replace primitive wrapper constructors with valueOf/autoboxing.",
                PrimitiveWrapperClassConstructorToValueOf::new
        );
        IssueDetector lambdaMethodRefDetector = new OpenRewriteRecipeDetector(
                "java:S1612",
                "Replace lambda with method reference where possible.",
                ReplaceLambdaWithMethodReference::new
        );
        IssueDetector bigDecimalEqualsSimpleDetector = new BigDecimalEqualsDetector();
        IssueDetector methodCouldBeStaticDetector = new MethodCouldBeStaticDetector();
        IssueDetector mathOperandsCastDetector = new MathOperandsCastDetector();
        IssueDetector simplifyBooleanDetector = new OpenRewriteRecipeDetector(
                "java:S2589",
                "Simplify gratuitous boolean expression.",
                SimplifyBooleanExpression::new
        );
        IssueDetector boxedBooleanDetector = new OpenRewriteRecipeDetector(
                "java:S5411",
                "Avoid boxed booleans in boolean expressions.",
                AvoidBoxedBooleanExpressions::new
        );
        IssueDetector resourcesShouldBeClosedDetector = new ResourcesShouldBeClosedDetector();

        IssueFixer unusedImportFixer = new UnusedImportFixer();
        IssueFixer collectionIsEmptyFixer = new OpenRewriteRecipeFixer("java:S1155", IsEmptyCallOnCollections::new);
        IssueFixer diamondOperatorFixer = new OpenRewriteRecipeFixer("java:S2293", UseDiamondOperatorSimpleRecipe::new);
        IssueFixer streamToListFixer = new OpenRewriteRecipeFixer("java:S6204", StreamCollectToListRecipe::new);
        IssueFixer redundantCastFixer = new OpenRewriteRecipeFixer("java:S1905", RemoveRedundantTypeCast::new);
        IssueFixer bigDecimalDoubleFixer = new OpenRewriteRecipeFixer("java:S2111", BigDecimalDoubleConstructorRecipe::new);
        IssueFixer standardOutputUsageFixer = new OpenRewriteRecipeFixer("java:S106", RemoveStandardOutputCallsRecipe::new);
        IssueFixer utilityClassConstructorFixer = new OpenRewriteRecipeFixer("java:S1118", HideUtilityClassConstructor::new);
        IssueFixer unusedLocalVariableFixer = new OpenRewriteRecipeFixer("java:S1481", () -> new RemoveUnusedLocalVariables(null, null));
        IssueFixer singleStatementLambdaFixer = new OpenRewriteRecipeFixer("java:S1602", LambdaBlockToExpression::new);
        IssueFixer literalsOnLeftEqualsFixer = new OpenRewriteRecipeFixer("java:S1132", EqualsAvoidsNull::new);
        IssueFixer indexOfContainsFixer = new OpenRewriteRecipeFixer("java:S3457", IndexOfReplaceableByContains::new);
        IssueFixer wrapperCtorFixer = new OpenRewriteRecipeFixer("java:S4970", PrimitiveWrapperClassConstructorToValueOf::new);
        IssueFixer lambdaMethodRefFixer = new OpenRewriteRecipeFixer("java:S1612", ReplaceLambdaWithMethodReference::new);
        IssueFixer bigDecimalEqualsFixer = new TextRegexIssueFixer(
                "java:S4348",
                "([A-Za-z_][A-Za-z0-9_]*)\\.equals\\(([^)]+)\\)",
                "$1.compareTo($2) == 0"
        );
        IssueFixer methodCouldBeStaticFixer = new TextRegexIssueFixer(
                "java:S2325",
                "private\\s+(?!static)([A-Za-z_][A-Za-z0-9_<>\\[\\],\\s?]*)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(",
                "private static $1 $2("
        );
        IssueFixer mathOperandsCastFixer = new TextRegexIssueFixer(
                "java:S2184",
                "(long|float|double)(\\s+[A-Za-z_][A-Za-z0-9_]*\\s*=\\s*)([A-Za-z_][A-Za-z0-9_]*)(\\s*[/+*-]\\s*[A-Za-z_][A-Za-z0-9_]*\\s*;)",
                "$1$2($1) $3$4"
        );
        IssueFixer simplifyBooleanFixer = new OpenRewriteRecipeFixer("java:S2589", SimplifyBooleanExpression::new);
        IssueFixer boxedBooleanFixer = new OpenRewriteRecipeFixer("java:S5411", AvoidBoxedBooleanExpressions::new);
        IssueFixer resourcesShouldBeClosedFixer = new TryWithResourcesRegexFixer();

        this.detectors = Map.ofEntries(
                entry(unusedImportDetector.ruleKey().value(), unusedImportDetector),
                entry(collectionIsEmptyDetector.ruleKey().value(), collectionIsEmptyDetector),
                entry(bigDecimalDoubleConstructorDetector.ruleKey().value(), bigDecimalDoubleConstructorDetector),
                entry(diamondOperatorDetector.ruleKey().value(), diamondOperatorDetector),
                entry(streamToListDetector.ruleKey().value(), streamToListDetector),
                entry(utilityClassConstructorDetector.ruleKey().value(), utilityClassConstructorDetector),
                entry(redundantCastDetector.ruleKey().value(), redundantCastDetector),
                entry(unusedLocalVariableDetector.ruleKey().value(), unusedLocalVariableDetector),
                entry(singleStatementLambdaDetector.ruleKey().value(), singleStatementLambdaDetector),
                entry(standardOutputUsageDetector.ruleKey().value(), standardOutputUsageDetector),
                entry(unusedPrivateFieldDetector.ruleKey().value(), unusedPrivateFieldDetector),
                entry(systemOutErrUsageDetector.ruleKey().value(), systemOutErrUsageDetector),
                entry(emptyNestedBlockDetector.ruleKey().value(), emptyNestedBlockDetector),
                entry(literalsOnLeftEqualsDetector.ruleKey().value(), literalsOnLeftEqualsDetector),
                entry(indexOfContainsDetector.ruleKey().value(), indexOfContainsDetector),
                entry(wrapperCtorDetector.ruleKey().value(), wrapperCtorDetector),
                entry(lambdaMethodRefDetector.ruleKey().value(), lambdaMethodRefDetector),
                entry(bigDecimalEqualsSimpleDetector.ruleKey().value(), bigDecimalEqualsSimpleDetector),
                entry(methodCouldBeStaticDetector.ruleKey().value(), methodCouldBeStaticDetector),
                entry(mathOperandsCastDetector.ruleKey().value(), mathOperandsCastDetector),
                entry(simplifyBooleanDetector.ruleKey().value(), simplifyBooleanDetector),
                entry(boxedBooleanDetector.ruleKey().value(), boxedBooleanDetector),
                entry(resourcesShouldBeClosedDetector.ruleKey().value(), resourcesShouldBeClosedDetector)
        );
        this.fixers = Map.ofEntries(
                entry(unusedImportFixer.ruleKey().value(), unusedImportFixer),
                entry(collectionIsEmptyFixer.ruleKey().value(), collectionIsEmptyFixer),
                entry(diamondOperatorFixer.ruleKey().value(), diamondOperatorFixer),
                entry(streamToListFixer.ruleKey().value(), streamToListFixer),
                entry(redundantCastFixer.ruleKey().value(), redundantCastFixer),
                entry(bigDecimalDoubleFixer.ruleKey().value(), bigDecimalDoubleFixer),
                entry(standardOutputUsageFixer.ruleKey().value(), standardOutputUsageFixer),
                entry(utilityClassConstructorFixer.ruleKey().value(), utilityClassConstructorFixer),
                entry(unusedLocalVariableFixer.ruleKey().value(), unusedLocalVariableFixer),
                entry(singleStatementLambdaFixer.ruleKey().value(), singleStatementLambdaFixer),
                entry(literalsOnLeftEqualsFixer.ruleKey().value(), literalsOnLeftEqualsFixer),
                entry(indexOfContainsFixer.ruleKey().value(), indexOfContainsFixer),
                entry(wrapperCtorFixer.ruleKey().value(), wrapperCtorFixer),
                entry(lambdaMethodRefFixer.ruleKey().value(), lambdaMethodRefFixer),
                entry(bigDecimalEqualsFixer.ruleKey().value(), bigDecimalEqualsFixer),
                entry(methodCouldBeStaticFixer.ruleKey().value(), methodCouldBeStaticFixer),
                entry(mathOperandsCastFixer.ruleKey().value(), mathOperandsCastFixer),
                entry(simplifyBooleanFixer.ruleKey().value(), simplifyBooleanFixer),
                entry(boxedBooleanFixer.ruleKey().value(), boxedBooleanFixer),
                entry(resourcesShouldBeClosedFixer.ruleKey().value(), resourcesShouldBeClosedFixer)
        );
        this.capabilities = List.of(
                new RuleCapability(unusedImportDetector.ruleKey(), true, true, "high"),
                new RuleCapability(collectionIsEmptyDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(bigDecimalDoubleConstructorDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(diamondOperatorDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(streamToListDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(utilityClassConstructorDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(redundantCastDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(unusedLocalVariableDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(singleStatementLambdaDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(standardOutputUsageDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(unusedPrivateFieldDetector.ruleKey(), true, false, "medium"),
                new RuleCapability(systemOutErrUsageDetector.ruleKey(), true, false, "medium"),
                new RuleCapability(emptyNestedBlockDetector.ruleKey(), true, false, "medium"),
                new RuleCapability(literalsOnLeftEqualsDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(indexOfContainsDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(wrapperCtorDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(lambdaMethodRefDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(bigDecimalEqualsSimpleDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(methodCouldBeStaticDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(mathOperandsCastDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(simplifyBooleanDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(boxedBooleanDetector.ruleKey(), true, true, "medium"),
                new RuleCapability(resourcesShouldBeClosedDetector.ruleKey(), true, true, "medium")
        );

        validateCapabilities();
    }

    private void validateCapabilities() {
        Set<String> allowedConfidence = Set.of("high", "medium", "low");
        for (RuleCapability capability : capabilities) {
            if (capability.ruleKey() == null || capability.ruleKey().value() == null || capability.ruleKey().value().isBlank()) {
                throw new IllegalStateException("Capability has invalid rule key");
            }
            if (!allowedConfidence.contains(capability.confidence())) {
                throw new IllegalStateException("Capability has invalid confidence for rule "
                        + capability.ruleKey().value() + ": " + capability.confidence());
            }
            if (capability.fixable() && !fixers.containsKey(capability.ruleKey().value())) {
                throw new IllegalStateException("Capability marked fixable but fixer is missing for rule " + capability.ruleKey().value());
            }
            if (capability.detectable() && !detectors.containsKey(capability.ruleKey().value())) {
                throw new IllegalStateException("Capability marked detectable but detector is missing for rule " + capability.ruleKey().value());
            }
        }
    }

    @Override
    public Optional<IssueDetector> detector(RuleKey key) {
        return Optional.ofNullable(detectors.get(key.value()));
    }

    @Override
    public Optional<IssueFixer> fixer(RuleKey key) {
        return Optional.ofNullable(fixers.get(key.value()));
    }

    @Override
    public List<RuleCapability> capabilities() {
        return capabilities;
    }
}
