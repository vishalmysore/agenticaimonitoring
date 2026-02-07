package io.github.vishalmysore.monitoring.service;

import io.github.vishalmysore.monitoring.analysis.DecisionPair;
import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MonitoringService - the main API entry point.
 */
class MonitoringServiceTest {

    private MonitoringService monitoring;

    @BeforeEach
    void setUp() {
        monitoring = new MonitoringService();
    }

    @Test
    void testRecordDecision() {
        DecisionRecord decision = monitoring.recordDecision(
                "test-scenario",
                "TestModel",
                DecisionMode.THEORY,
                "option_a",
                "Test reasoning",
                8.5);

        assertNotNull(decision);
        assertNotNull(decision.getDecisionId());
        assertEquals("test-scenario", decision.getScenarioId());
        assertEquals("TestModel", decision.getModelName());
        assertEquals(DecisionMode.THEORY, decision.getMode());
        assertEquals("option_a", decision.getChoice());
        assertEquals(8.5, decision.getConfidence());
    }

    @Test
    void testRecordDecisionWithProvidedRecord() {
        DecisionRecord decision = DecisionRecord.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .mode(DecisionMode.ACTION)
                .choice("choice_b")
                .reasoning("reasoning")
                .confidence(7.0)
                .build();

        DecisionRecord recorded = monitoring.recordDecision(decision);

        assertNotNull(recorded.getDecisionId());
        assertEquals("scenario-1", recorded.getScenarioId());
    }

    @Test
    void testAnalyzeReversals() {
        // Record reversal
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY,
                "choice_a", "reasoning", 8.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION,
                "choice_b", "reasoning", 7.0);

        List<DecisionPair> reversals = monitoring.analyzeReversals("scenario-1");

        assertEquals(1, reversals.size());
        assertTrue(reversals.get(0).isReversal());
    }

    @Test
    void testAnalyzeReversalsNoReversal() {
        // Record consistent decisions
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY,
                "same_choice", "reasoning", 8.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION,
                "same_choice", "reasoning", 7.5);

        List<DecisionPair> reversals = monitoring.analyzeReversals("scenario-1");

        assertEquals(0, reversals.size());
    }

    @Test
    void testCheckConsensus() {
        // Create consensus in theory mode
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model2", DecisionMode.THEORY, "choice_a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model3", DecisionMode.THEORY, "choice_a", "r", 8.0);

        // No consensus in action mode
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_a", "r", 7.0);
        monitoring.recordDecision("scenario-1", "Model2", DecisionMode.ACTION, "choice_b", "r", 7.0);
        monitoring.recordDecision("scenario-1", "Model3", DecisionMode.ACTION, "choice_c", "r", 7.0);

        var comparison = monitoring.checkConsensus("scenario-1");

        assertTrue(comparison.isConsensusCollapsed());
        assertTrue(comparison.getTheoryConsensus().hasConsensus());
        assertFalse(comparison.getActionConsensus().hasConsensus());
    }

    @Test
    void testGetOverallReversalRate() {
        // Scenario 1: 1 reversal
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY, "a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION, "b", "r", 7.0);

        // Scenario 2: 0 reversals
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.THEORY, "x", "r", 8.0);
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.ACTION, "x", "r", 7.5);

        double rate = monitoring.getOverallReversalRate();
        assertEquals(0.5, rate, 0.01); // 1 out of 2 = 50%
    }

    @Test
    void testGetModelReversalRate() {
        // Model1: 2 reversals
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY, "a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION, "b", "r", 7.0);
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.THEORY, "x", "r", 8.0);
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.ACTION, "y", "r", 7.5);

        // Model2: 0 reversals
        monitoring.recordDecision("scenario-1", "Model2", DecisionMode.THEORY, "z", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model2", DecisionMode.ACTION, "z", "r", 7.5);

        double model1Rate = monitoring.getModelReversalRate("Model1");
        assertEquals(1.0, model1Rate, 0.01); // 100%

        double model2Rate = monitoring.getModelReversalRate("Model2");
        assertEquals(0.0, model2Rate, 0.01); // 0%
    }

    @Test
    void testGetOverallConfidenceDrop() {
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY, "a", "r", 9.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION, "b", "r", 7.0);

        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.THEORY, "x", "r", 8.0);
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.ACTION, "y", "r", 7.0);

        double drop = monitoring.getOverallConfidenceDrop();
        assertEquals(1.5, drop, 0.01); // Average of 2.0 and 1.0
    }

    @Test
    void testGetConsensusCollapseRate() {
        // Scenario 1: Collapse
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY, "a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model2", DecisionMode.THEORY, "a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model3", DecisionMode.THEORY, "a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION, "a", "r", 7.0);
        monitoring.recordDecision("scenario-1", "Model2", DecisionMode.ACTION, "b", "r", 7.0);
        monitoring.recordDecision("scenario-1", "Model3", DecisionMode.ACTION, "c", "r", 7.0);

        // Scenario 2: No collapse
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.THEORY, "x", "r", 8.0);
        monitoring.recordDecision("scenario-2", "Model2", DecisionMode.THEORY, "x", "r", 8.0);
        monitoring.recordDecision("scenario-2", "Model1", DecisionMode.ACTION, "x", "r", 7.0);
        monitoring.recordDecision("scenario-2", "Model2", DecisionMode.ACTION, "x", "r", 7.0);

        double rate = monitoring.getConsensusCollapseRate();
        assertEquals(0.5, rate, 0.01); // 1 out of 2 = 50%
    }

    @Test
    void testClear() {
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.THEORY, "a", "r", 8.0);
        monitoring.recordDecision("scenario-1", "Model1", DecisionMode.ACTION, "b", "r", 7.0);

        assertEquals(2, monitoring.getRepository().count());

        monitoring.clear();

        assertEquals(0, monitoring.getRepository().count());
    }

    @Test
    void testMultipleModelsMultipleScenarios() {
        // Complex scenario with multiple models and scenarios
        String[] models = { "GPT-5", "Claude-4", "Gemini-3" };
        String[] scenarios = { "medical", "financial", "legal" };

        for (String scenario : scenarios) {
            for (String model : models) {
                monitoring.recordDecision(scenario, model, DecisionMode.THEORY, "theory_choice", "r", 8.5);
                monitoring.recordDecision(scenario, model, DecisionMode.ACTION, "action_choice", "r", 7.5);
            }
        }

        // Should have 18 total decisions (3 models × 3 scenarios × 2 modes)
        assertEquals(18, monitoring.getRepository().count());

        // All should be reversals
        double overallRate = monitoring.getOverallReversalRate();
        assertEquals(1.0, overallRate, 0.01); // 100%
    }
}
