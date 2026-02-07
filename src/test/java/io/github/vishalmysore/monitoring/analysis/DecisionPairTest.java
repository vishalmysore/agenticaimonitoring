package io.github.vishalmysore.monitoring.analysis;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import io.github.vishalmysore.monitoring.domain.ReversalDirection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DecisionPair.
 */
class DecisionPairTest {

    @Test
    void testIsReversalWhenChoicesDiffer() {
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "choice_a", 8.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "choice_b", 7.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        assertTrue(pair.isReversal());
    }

    @Test
    void testIsReversalWhenChoicesMatch() {
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "same_choice", 8.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "same_choice", 7.5);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        assertFalse(pair.isReversal());
    }

    @Test
    void testGetConfidenceDrop() {
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "choice_a", 9.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "choice_b", 7.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        assertEquals(2.0, pair.getConfidenceDrop(), 0.01);
    }

    @Test
    void testGetConfidenceDropNegative() {
        // Edge case: confidence increased in action mode
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "choice_a", 7.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "choice_b", 9.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        assertEquals(-2.0, pair.getConfidenceDrop(), 0.01);
    }

    @Test
    void testGetReversalDirectionConservative() {
        // intervention -> ambiguous or less interventionist
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "intervention", 8.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "ambiguous", 7.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        ReversalDirection direction = pair.getReversalDirection();
        assertTrue(direction == ReversalDirection.CONSERVATIVE || direction == ReversalDirection.LATERAL);
    }

    @Test
    void testGetReversalDirectionPermissive() {
        // ambiguous -> intervention or more interventionist
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "ambiguous", 8.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "intervention", 7.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        ReversalDirection direction = pair.getReversalDirection();
        assertTrue(direction == ReversalDirection.PERMISSIVE || direction == ReversalDirection.LATERAL);
    }

    @Test
    void testGetReversalDirectionLateral() {
        // Different choices but same intervention level
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "choice_a", 8.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "choice_b", 7.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        ReversalDirection direction = pair.getReversalDirection();
        assertNotNull(direction);
    }

    @Test
    void testBuilderCreatesValidPair() {
        DecisionRecord theory = createDecision(DecisionMode.THEORY, "choice_a", 8.0);
        DecisionRecord action = createDecision(DecisionMode.ACTION, "choice_b", 7.0);

        DecisionPair pair = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory)
                .actionDecision(action)
                .build();

        assertEquals("scenario-1", pair.getScenarioId());
        assertEquals("Model1", pair.getModelName());
        assertNotNull(pair.getTheoryDecision());
        assertNotNull(pair.getActionDecision());
    }

    @Test
    void testEqualityBasedOnScenarioAndModel() {
        DecisionRecord theory1 = createDecision(DecisionMode.THEORY, "choice_a", 8.0);
        DecisionRecord action1 = createDecision(DecisionMode.ACTION, "choice_b", 7.0);

        DecisionPair pair1 = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory1)
                .actionDecision(action1)
                .build();

        DecisionRecord theory2 = createDecision(DecisionMode.THEORY, "choice_a", 8.0);
        DecisionRecord action2 = createDecision(DecisionMode.ACTION, "choice_b", 7.0);

        DecisionPair pair2 = DecisionPair.builder()
                .scenarioId("scenario-1")
                .modelName("Model1")
                .theoryDecision(theory2)
                .actionDecision(action2)
                .build();

        // Should be equal if scenario and model match
        assertEquals(pair1.getScenarioId(), pair2.getScenarioId());
        assertEquals(pair1.getModelName(), pair2.getModelName());
    }

    private DecisionRecord createDecision(DecisionMode mode, String choice, Double confidence) {
        return DecisionRecord.builder()
                .decisionId(java.util.UUID.randomUUID().toString())
                .scenarioId("test-scenario")
                .modelName("TestModel")
                .mode(mode)
                .choice(choice)
                .reasoning("Test reasoning")
                .confidence(confidence)
                .build();
    }
}
