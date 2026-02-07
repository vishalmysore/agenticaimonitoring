package io.github.vishalmysore.monitoring;

import io.github.vishalmysore.monitoring.analysis.DecisionPair;
import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify the monitoring system works correctly.
 */
public class MonitoringSystemTest {

    private MonitoringService monitoring;

    @BeforeEach
    public void setUp() {
        monitoring = new MonitoringService();
    }

    @Test
    public void testBasicDecisionRecording() {
        // Record two decisions for the same scenario in different modes
        monitoring.recordDecision(
                "test-scenario",
                "TestModel",
                DecisionMode.THEORY,
                "option_a",
                "This seems like the right choice",
                8.0);

        monitoring.recordDecision(
                "test-scenario",
                "TestModel",
                DecisionMode.ACTION,
                "option_b",
                "On second thought, this is safer",
                7.5);

        // Verify decisions were recorded
        assertEquals(2, monitoring.getRepository().count());
    }

    @Test
    public void testReversalDetection() {
        // Record a reversal (different choices between modes)
        monitoring.recordDecision("scenario1", "Model1", DecisionMode.THEORY,
                "choice_a", "reasoning", 8.0);
        monitoring.recordDecision("scenario1", "Model1", DecisionMode.ACTION,
                "choice_b", "reasoning", 7.0);

        // Detect reversals
        List<DecisionPair> reversals = monitoring.analyzeReversals("scenario1");

        assertEquals(1, reversals.size());
        assertTrue(reversals.get(0).isReversal());
    }

    @Test
    public void testNoReversalWhenChoicesMatch() {
        // Record consistent choices
        monitoring.recordDecision("scenario2", "Model2", DecisionMode.THEORY,
                "same_choice", "reasoning", 8.0);
        monitoring.recordDecision("scenario2", "Model2", DecisionMode.ACTION,
                "same_choice", "reasoning", 8.0);

        // Should not detect a reversal
        List<DecisionPair> reversals = monitoring.analyzeReversals("scenario2");

        assertEquals(0, reversals.size());
    }

    @Test
    public void testConfidenceDrop() {
        monitoring.recordDecision("scenario3", "Model3", DecisionMode.THEORY,
                "choice_a", "reasoning", 9.0);
        monitoring.recordDecision("scenario3", "Model3", DecisionMode.ACTION,
                "choice_b", "reasoning", 7.0);

        double confidenceDrop = monitoring.getReversalDetector()
                .calculateAverageConfidenceDrop("scenario3");

        assertEquals(2.0, confidenceDrop, 0.01);
    }

    @Test
    public void testReversalRate() {
        // Create 2 models, 1 reverses and 1 doesn't
        monitoring.recordDecision("scenario4", "ModelA", DecisionMode.THEORY,
                "choice_a", "reasoning", 8.0);
        monitoring.recordDecision("scenario4", "ModelA", DecisionMode.ACTION,
                "choice_b", "reasoning", 7.0);

        monitoring.recordDecision("scenario4", "ModelB", DecisionMode.THEORY,
                "choice_x", "reasoning", 8.0);
        monitoring.recordDecision("scenario4", "ModelB", DecisionMode.ACTION,
                "choice_x", "reasoning", 8.0);

        double reversalRate = monitoring.getReversalDetector()
                .calculateReversalRate("scenario4");

        assertEquals(0.5, reversalRate, 0.01); // 50% reversal rate
    }
}
