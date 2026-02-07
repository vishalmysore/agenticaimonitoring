package io.github.vishalmysore.monitoring.analysis;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import io.github.vishalmysore.monitoring.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConsensusAnalyzer.
 */
class ConsensusAnalyzerTest {

    private DecisionRepository repository;
    private ConsensusAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        repository = new DecisionRepository();
        analyzer = new ConsensusAnalyzer(repository);
    }

    @Test
    void testCheckConsensusWithUnanimousAgreement() {
        String scenarioId = "scenario-1";

        // All 3 models choose "choice_a" in theory mode
        repository.save(createDecision(scenarioId, "Model1", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model3", DecisionMode.THEORY, "choice_a"));

        ConsensusAnalyzer.ConsensusResult consensus = analyzer.checkConsensus(scenarioId, DecisionMode.THEORY);

        assertTrue(consensus.hasConsensus());
        assertEquals("choice_a", consensus.getConsensusChoice());
        assertEquals(1.0, consensus.getConsensusPercentage(), 0.01); // 100%
        assertEquals(3, consensus.getConsensusCount());
    }

    @Test
    void testCheckConsensusWithSupermajority() {
        String scenarioId = "scenario-1";

        // 3 out of 4 choose "choice_a"
        repository.save(createDecision(scenarioId, "Model1", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model3", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model4", DecisionMode.THEORY, "choice_b"));

        ConsensusAnalyzer.ConsensusResult consensus = analyzer.checkConsensus(scenarioId, DecisionMode.THEORY);

        assertTrue(consensus.hasConsensus());
        assertEquals("choice_a", consensus.getConsensusChoice());
        assertEquals(0.75, consensus.getConsensusPercentage(), 0.01); // 75%
    }

    @Test
    void testCheckConsensusWithNoConsensus() {
        String scenarioId = "scenario-1";

        // Evenly split - no supermajority
        repository.save(createDecision(scenarioId, "Model1", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.THEORY, "choice_b"));

        ConsensusAnalyzer.ConsensusResult consensus = analyzer.checkConsensus(scenarioId, DecisionMode.THEORY);

        assertFalse(consensus.hasConsensus());
        assertNull(consensus.getConsensusChoice());
    }

    @Test
    void testCompareConsensusNoCollapse() {
        String scenarioId = "scenario-1";

        // Consensus in both modes
        repository.save(createDecision(scenarioId, "Model1", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model3", DecisionMode.THEORY, "choice_a"));

        repository.save(createDecision(scenarioId, "Model1", DecisionMode.ACTION, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.ACTION, "choice_a"));
        repository.save(createDecision(scenarioId, "Model3", DecisionMode.ACTION, "choice_a"));

        ConsensusAnalyzer.ConsensusComparison comparison = analyzer.compareConsensus(scenarioId);

        assertFalse(comparison.isConsensusCollapsed());
        assertTrue(comparison.getTheoryConsensus().hasConsensus());
        assertTrue(comparison.getActionConsensus().hasConsensus());
    }

    @Test
    void testCompareConsensusWithCollapse() {
        String scenarioId = "scenario-1";

        // Consensus in theory mode
        repository.save(createDecision(scenarioId, "Model1", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision(scenarioId, "Model3", DecisionMode.THEORY, "choice_a"));

        // No consensus in action mode (all different)
        repository.save(createDecision(scenarioId, "Model1", DecisionMode.ACTION, "choice_a"));
        repository.save(createDecision(scenarioId, "Model2", DecisionMode.ACTION, "choice_b"));
        repository.save(createDecision(scenarioId, "Model3", DecisionMode.ACTION, "choice_c"));

        ConsensusAnalyzer.ConsensusComparison comparison = analyzer.compareConsensus(scenarioId);

        assertTrue(comparison.isConsensusCollapsed());
        assertTrue(comparison.getTheoryConsensus().hasConsensus());
        assertFalse(comparison.getActionConsensus().hasConsensus());
    }

    @Test
    void testCalculateConsensusCollapseRate() {
        // Scenario 1: No collapse
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision("scenario-1", "Model3", DecisionMode.THEORY, "choice_a"));
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.ACTION, "choice_a"));
        repository.save(createDecision("scenario-1", "Model2", DecisionMode.ACTION, "choice_a"));
        repository.save(createDecision("scenario-1", "Model3", DecisionMode.ACTION, "choice_a"));

        // Scenario 2: Collapse
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.THEORY, "choice_x"));
        repository.save(createDecision("scenario-2", "Model2", DecisionMode.THEORY, "choice_x"));
        repository.save(createDecision("scenario-2", "Model3", DecisionMode.THEORY, "choice_x"));
        repository.save(createDecision("scenario-2", "Model1", DecisionMode.ACTION, "choice_a"));
        repository.save(createDecision("scenario-2", "Model2", DecisionMode.ACTION, "choice_b"));
        repository.save(createDecision("scenario-2", "Model3", DecisionMode.ACTION, "choice_c"));

        double collapseRate = analyzer.calculateConsensusCollapseRate();
        assertEquals(0.5, collapseRate, 0.01); // 1 out of 2 = 50%
    }

    @Test
    void testCheckConsensusWithSingleModel() {
        // Edge case: only 1 model (should have 100% consensus)
        repository.save(createDecision("scenario-1", "Model1", DecisionMode.THEORY, "choice_a"));

        ConsensusAnalyzer.ConsensusResult consensus = analyzer.checkConsensus("scenario-1", DecisionMode.THEORY);

        assertTrue(consensus.hasConsensus());
        assertEquals(1.0, consensus.getConsensusPercentage(), 0.01);
    }

    @Test
    void testCheckConsensusWithNoDecisions() {
        ConsensusAnalyzer.ConsensusResult consensus = analyzer.checkConsensus("nonexistent", DecisionMode.THEORY);

        assertFalse(consensus.hasConsensus());
        assertEquals(0, consensus.getTotalDecisions());
    }

    private DecisionRecord createDecision(String scenarioId, String modelName, DecisionMode mode, String choice) {
        return DecisionRecord.builder()
                .decisionId(java.util.UUID.randomUUID().toString())
                .scenarioId(scenarioId)
                .modelName(modelName)
                .mode(mode)
                .choice(choice)
                .reasoning("Test reasoning")
                .confidence(8.0)
                .build();
    }
}
