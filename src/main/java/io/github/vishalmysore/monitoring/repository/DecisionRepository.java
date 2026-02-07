package io.github.vishalmysore.monitoring.repository;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for storing and querying decision records.
 * Thread-safe for concurrent monitoring scenarios.
 */
@Log
public class DecisionRepository {

    // Primary storage: Map<DecisionId, DecisionRecord>
    private final Map<String, DecisionRecord> decisionsById = new ConcurrentHashMap<>();

    // Index by scenario: Map<ScenarioId, List<DecisionRecord>>
    private final Map<String, List<DecisionRecord>> decisionsByScenario = new ConcurrentHashMap<>();

    // Index by model: Map<ModelName, List<DecisionRecord>>
    private final Map<String, List<DecisionRecord>> decisionsByModel = new ConcurrentHashMap<>();

    // Index by scenario + model + mode: Map<Key, DecisionRecord>
    private final Map<String, DecisionRecord> decisionsByScenarioModelMode = new ConcurrentHashMap<>();

    /**
     * Saves a decision record to the repository.
     */
    public void save(DecisionRecord decision) {
        if (decision == null || decision.getDecisionId() == null) {
            throw new IllegalArgumentException("Decision and decisionId must not be null");
        }

        // Save to primary storage
        decisionsById.put(decision.getDecisionId(), decision);

        // Update scenario index
        decisionsByScenario.computeIfAbsent(decision.getScenarioId(), k -> new ArrayList<>())
                .add(decision);

        // Update model index
        decisionsByModel.computeIfAbsent(decision.getModelName(), k -> new ArrayList<>())
                .add(decision);

        // Update composite index
        String compositeKey = buildCompositeKey(decision.getScenarioId(),
                decision.getModelName(),
                decision.getMode());
        decisionsByScenarioModelMode.put(compositeKey, decision);

        log.info(String.format("Saved decision: %s [%s/%s/%s] -> %s",
                decision.getDecisionId(),
                decision.getScenarioId(),
                decision.getModelName(),
                decision.getMode(),
                decision.getChoice()));
    }

    /**
     * Finds a decision by its ID.
     */
    public Optional<DecisionRecord> findById(String decisionId) {
        return Optional.ofNullable(decisionsById.get(decisionId));
    }

    /**
     * Finds all decisions for a given scenario.
     */
    public List<DecisionRecord> findByScenario(String scenarioId) {
        return new ArrayList<>(decisionsByScenario.getOrDefault(scenarioId, Collections.emptyList()));
    }

    /**
     * Finds all decisions made by a specific model.
     */
    public List<DecisionRecord> findByModel(String modelName) {
        return new ArrayList<>(decisionsByModel.getOrDefault(modelName, Collections.emptyList()));
    }

    /**
     * Finds a specific decision by scenario, model, and mode.
     */
    public Optional<DecisionRecord> findByScenarioModelMode(String scenarioId, String modelName, DecisionMode mode) {
        String key = buildCompositeKey(scenarioId, modelName, mode);
        return Optional.ofNullable(decisionsByScenarioModelMode.get(key));
    }

    /**
     * Finds all decisions for a scenario in a specific mode.
     */
    public List<DecisionRecord> findByScenarioAndMode(String scenarioId, DecisionMode mode) {
        return decisionsByScenario.getOrDefault(scenarioId, Collections.emptyList())
                .stream()
                .filter(d -> d.getMode() == mode)
                .collect(Collectors.toList());
    }

    /**
     * Gets all unique scenario IDs.
     */
    public Set<String> getAllScenarioIds() {
        return new HashSet<>(decisionsByScenario.keySet());
    }

    /**
     * Gets all unique model names.
     */
    public Set<String> getAllModelNames() {
        return new HashSet<>(decisionsByModel.keySet());
    }

    /**
     * Gets all decisions (for reporting purposes).
     */
    public List<DecisionRecord> findAll() {
        return new ArrayList<>(decisionsById.values());
    }

    /**
     * Clears all decisions (useful for testing).
     */
    public void clear() {
        decisionsById.clear();
        decisionsByScenario.clear();
        decisionsByModel.clear();
        decisionsByScenarioModelMode.clear();
        log.info("Repository cleared");
    }

    /**
     * Gets the total count of decisions.
     */
    public int count() {
        return decisionsById.size();
    }

    /**
     * Builds a composite key for indexing.
     */
    private String buildCompositeKey(String scenarioId, String modelName, DecisionMode mode) {
        return String.format("%s:%s:%s", scenarioId, modelName, mode);
    }
}
