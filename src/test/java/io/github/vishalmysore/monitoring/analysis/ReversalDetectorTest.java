package io.github.vishalmysore.monitoring.analysis;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import io.github.vishalmysore.monitoring.domain.ReversalDirection;
import io.github.vishalmysore.monitoring.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReversalDetector.
 */
class ReversalDetectorTest {

    private DecisionRepository repository;
    private ReversalDetector detector;

    @BeforeEach
    void setUp() {
        repository = new DecisionRepository();
        detector = new ReversalDetector(repository);
    }

    @Test
    void testFindPairsWithBothModes() {
        // Add theory and action decisions
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));

        List<DecisionPair> pairs = detector.findPairs("scenario-1");
        assertEquals(1, pairs.size());
        assertEquals("Model1", pairs.get(0).getModelName());
    }

    @Test
    void testFindPairsWithOnlyTheory() {
        // Only theory decision - should not create a pair
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 8.0));

        List<DecisionPair> pairs = detector.findPairs("scenario-1");
        assertEquals(0, pairs.size());
    }

    @Test
    void testFindPairsWithOnlyAction() {
        // Only action decision - should not create a pair
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_a", 8.0));

        List<DecisionPair> pairs = detector.findPairs("scenario-1");
        assertEquals(0, pairs.size());
    }

    @Test
    void testDetectReversalsWhenChoicesDiffer() {
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));

        List<DecisionPair> reversals = detector.detectReversals("scenario-1");
        assertEquals(1, reversals.size());
        assertTrue(reversals.get(0).isReversal());
    }

    @Test
    void testDetectReversalsWhenChoicesMatch() {
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "same_choice", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "same_choice", 7.5));

        List<DecisionPair> reversals = detector.detectReversals("scenario-1");
        assertEquals(0, reversals.size());
    }

    @Test
    void testCalculateReversalRate() {
        // 2 models: one reverses, one doesn't
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));

        repository.save(createDecision("scenario-1", "Model2", DecisionMode.THEORY, "choice_x", 8.0));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.ACTION, "choice_x", 7.5));

        double rate = detector.calculateReversalRate("scenario-1");
        assertEquals(0.5, rate, 0.01); // 1 out of 2 = 50%
    }

    @Test
    void testCalculateReversalRateNoDecisions() {
        double rate = detector.calculateReversalRate("nonexistent");
        assertEquals(0.0, rate, 0.01);
    }

    @Test
    void testCalculateOverallReversalRate() {
        // Scenario 1: 1/2 reversals (50%)
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.THEORY, "choice_x", 8.0));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.ACTION, "choice_x", 7.5));

        // Scenario 2: 2/2 reversals (100%)
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.THEORY, "choice_a", 8.0));
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.ACTION, "choice_b", 7.0));
        repository.save(createDecision("scenario-2", "Model2", DecisionMode.THEORY, "choice_x", 8.0));
        repository.save(createDecision("scenario-2", "Model2", DecisionMode.ACTION, "choice_y", 7.5));

        double rate = detector.calculateOverallReversalRate();
        assertEquals(0.75, rate, 0.01); // 3 out of 4 = 75%
    }

    @Test
    void testCalculateModelReversalRate() {
        // Model1: 2/2 reversals
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.THEORY, "choice_x", 8.0));
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.ACTION, "choice_y", 7.5));

        // Model2: 0/1 reversals
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.THEORY, "choice_z", 8.0));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.ACTION, "choice_z", 7.5));

        double model1Rate = detector.calculateModelReversalRate("Model1");
        assertEquals(1.0, model1Rate, 0.01); // 100%

        double model2Rate = detector.calculateModelReversalRate("Model2");
        assertEquals(0.0, model2Rate, 0.01); // 0%
    }

    @Test
    void testCalculateAverageConfidenceDrop() {
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 9.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));

        repository.save(createDecision("scenario-1", "Model2", DecisionMode.THEORY, "choice_x", 8.0));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.ACTION, "choice_y", 6.0));

        double drop = detector.calculateAverageConfidenceDrop("scenario-1");
        assertEquals(2.0, drop, 0.01); // Average of (9-7) and (8-6) = 2.0
    }

    @Test
    void testCalculateOverallConfidenceDrop() {
        // Scenario 1
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a", 9.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_b", 7.0));

        // Scenario 2
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.THEORY, "choice_x", 10.0));
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.ACTION, "choice_y", 9.0));

        double drop = detector.calculateOverallConfidenceDrop();
        assertEquals(1.5, drop, 0.01); // Average of 2.0 and 1.0 = 1.5
    }

    @Test
    void testAnalyzeReversalDirections() {
        // Add decisions with different reversal patterns
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "intervene", 8.0));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "defer", 7.0));

        repository.save(createDecision("scenario-1", "Model2", DecisionMode.THEORY, "defer", 8.0));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.ACTION, "intervene", 7.0));

        Map<ReversalDirection, Long> directions = detector.analyzeReversalDirections("scenario-1");

        assertTrue(directions.containsKey(ReversalDirection.CONSERVATIVE) ||
                directions.containsKey(ReversalDirection.PERMISSIVE) ||
                directions.containsKey(ReversalDirection.LATERAL));
    }

    private DecisionRecord createDecision(String scenarioId, String modelName, DecisionMode mode,
            String choice, Double confidence) {
        return DecisionRecord.builder()
                .decisionId(java.util.UUID.randomUUID().toString())
                .scenarioId(scenarioId)
                .modelName(modelName)
                .mode(mode)
                .choice(choice)
                .reasoning("Test reasoning")
                .confidence(confidence)
                .build();
    }
}
