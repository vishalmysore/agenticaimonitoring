package io.github.vishalmysore.monitoring.repository;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DecisionRepository.
 */
class DecisionRepositoryTest {

    private DecisionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DecisionRepository();
    }

    @Test
    void testSaveAndCount() {
        DecisionRecord decision = DecisionRecord.builder()
                .decisionId("test-1")
                .scenarioId("scenario-1")
                .modelName("TestModel")
                .mode(DecisionMode.THEORY)
                .choice("option_a")
                .reasoning("Test reasoning")
                .confidence(8.5)
                .build();

        repository.save(decision);
        assertEquals(1, repository.count());
    }

    @Test
    void testFindById() {
        DecisionRecord decision = DecisionRecord.builder()
                .decisionId("test-2")
                .scenarioId("scenario-1")
                .modelName("TestModel")
                .mode(DecisionMode.THEORY)
                .choice("option_a")
                .reasoning("Test reasoning")
                .confidence(8.5)
                .build();

        repository.save(decision);

        Optional<DecisionRecord> found = repository.findById("test-2");
        assertTrue(found.isPresent());
        assertEquals("option_a", found.get().getChoice());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<DecisionRecord> found = repository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByScenario() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-1", "Model2", DecisionMode.ACTION));
        repository.save(createDecision("d3", "scenario-2", "Model1", DecisionMode.THEORY));

        List<DecisionRecord> results = repository.findByScenario("scenario-1");
        assertEquals(2, results.size());
    }

    @Test
    void testFindByModel() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-2", "Model1", DecisionMode.ACTION));
        repository.save(createDecision("d3", "scenario-1", "Model2", DecisionMode.THEORY));

        List<DecisionRecord> results = repository.findByModel("Model1");
        assertEquals(2, results.size());
    }

    @Test
    void testFindByMode() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-1", "Model2", DecisionMode.THEORY));
        repository.save(createDecision("d3", "scenario-1", "Model1", DecisionMode.ACTION));

        List<DecisionRecord> results = repository.findByMode(DecisionMode.THEORY);
        assertEquals(2, results.size());
    }

    @Test
    void testFindByScenarioModelMode() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-1", "Model1", DecisionMode.ACTION));
        repository.save(createDecision("d3", "scenario-1", "Model2", DecisionMode.THEORY));

        Optional<DecisionRecord> found = repository.findByScenarioModelMode(
                "scenario-1", "Model1", DecisionMode.THEORY);

        assertTrue(found.isPresent());
        assertEquals("d1", found.get().getDecisionId());
    }

    @Test
    void testGetAllScenarioIds() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-2", "Model1", DecisionMode.ACTION));
        repository.save(createDecision("d3", "scenario-1", "Model2", DecisionMode.THEORY));

        Set<String> scenarioIds = repository.getAllScenarioIds();
        assertEquals(2, scenarioIds.size());
        assertTrue(scenarioIds.contains("scenario-1"));
        assertTrue(scenarioIds.contains("scenario-2"));
    }

    @Test
    void testGetAllModelNames() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-1", "Model2", DecisionMode.ACTION));
        repository.save(createDecision("d3", "scenario-2", "Model1", DecisionMode.THEORY));

        Set<String> modelNames = repository.getAllModelNames();
        assertEquals(2, modelNames.size());
        assertTrue(modelNames.contains("Model1"));
        assertTrue(modelNames.contains("Model2"));
    }

    @Test
    void testClear() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-1", "Model2", DecisionMode.ACTION));

        assertEquals(2, repository.count());

        repository.clear();
        assertEquals(0, repository.count());
    }

    @Test
    void testFindAll() {
        repository.save(createDecision("d1", "scenario-1", "Model1", DecisionMode.THEORY));
        repository.save(createDecision("d2", "scenario-1", "Model2", DecisionMode.ACTION));

        List<DecisionRecord> all = repository.findAll();
        assertEquals(2, all.size());
    }

    private DecisionRecord createDecision(String id, String scenarioId, String modelName, DecisionMode mode) {
        return DecisionRecord.builder()
                .decisionId(id)
                .scenarioId(scenarioId)
                .modelName(modelName)
                .mode(mode)
                .choice("test_choice")
                .reasoning("test reasoning")
                .confidence(8.0)
                .build();
    }
}
