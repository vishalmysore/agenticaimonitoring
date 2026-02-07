package io.github.vishalmysore.monitoring.analysis;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import io.github.vishalmysore.monitoring.domain.ReversalDirection;
import io.github.vishalmysore.monitoring.repository.DecisionRepository;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes decision pairs to detect reversals and behavioral shifts.
 * 
 * Based on the research finding that models reverse 47.6% of ethical decisions
 * when transitioning from theory to action mode.
 */
@Log
public class ReversalDetector {

    private final DecisionRepository repository;

    public ReversalDetector(DecisionRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds all decision pairs for a given scenario.
     * Only includes pairs where both theory and action decisions exist.
     */
    public List<DecisionPair> findPairs(String scenarioId) {
        List<DecisionPair> pairs = new ArrayList<>();

        // Get all models that made decisions for this scenario
        Set<String> models = repository.findByScenario(scenarioId)
                .stream()
                .map(DecisionRecord::getModelName)
                .collect(Collectors.toSet());

        // For each model, try to pair theory and action decisions
        for (String model : models) {
            Optional<DecisionRecord> theoryDecision = repository.findByScenarioModelMode(scenarioId, model,
                    DecisionMode.THEORY);
            Optional<DecisionRecord> actionDecision = repository.findByScenarioModelMode(scenarioId, model,
                    DecisionMode.ACTION);

            // Only create pair if both decisions exist
            if (theoryDecision.isPresent() && actionDecision.isPresent()) {
                DecisionPair pair = DecisionPair.builder()
                        .scenarioId(scenarioId)
                        .modelName(model)
                        .theoryDecision(theoryDecision.get())
                        .actionDecision(actionDecision.get())
                        .build();
                pairs.add(pair);
            }
        }

        return pairs;
    }

    /**
     * Detects reversals for a specific scenario.
     * Returns only pairs where the decision changed between modes.
     */
    public List<DecisionPair> detectReversals(String scenarioId) {
        return findPairs(scenarioId)
                .stream()
                .filter(DecisionPair::isReversal)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the reversal rate for a scenario.
     * Returns value between 0.0 and 1.0.
     */
    public double calculateReversalRate(String scenarioId) {
        List<DecisionPair> allPairs = findPairs(scenarioId);
        if (allPairs.isEmpty()) {
            return 0.0;
        }

        long reversalCount = allPairs.stream()
                .filter(DecisionPair::isReversal)
                .count();

        return (double) reversalCount / allPairs.size();
    }

    /**
     * Calculates reversal rate across all scenarios.
     */
    public double calculateOverallReversalRate() {
        Set<String> allScenarios = repository.getAllScenarioIds();

        int totalPairs = 0;
        int totalReversals = 0;

        for (String scenarioId : allScenarios) {
            List<DecisionPair> pairs = findPairs(scenarioId);
            totalPairs += pairs.size();
            totalReversals += (int) pairs.stream().filter(DecisionPair::isReversal).count();
        }

        return totalPairs == 0 ? 0.0 : (double) totalReversals / totalPairs;
    }

    /**
     * Calculates reversal rate for a specific model across all scenarios.
     */
    public double calculateModelReversalRate(String modelName) {
        Set<String> allScenarios = repository.getAllScenarioIds();

        int totalPairs = 0;
        int totalReversals = 0;

        for (String scenarioId : allScenarios) {
            List<DecisionPair> pairs = findPairs(scenarioId)
                    .stream()
                    .filter(p -> p.getModelName().equals(modelName))
                    .collect(Collectors.toList());

            totalPairs += pairs.size();
            totalReversals += (int) pairs.stream().filter(DecisionPair::isReversal).count();
        }

        return totalPairs == 0 ? 0.0 : (double) totalReversals / totalPairs;
    }

    /**
     * Analyzes reversal directions for a scenario.
     * Returns a map of direction -> count.
     */
    public Map<ReversalDirection, Long> analyzeReversalDirections(String scenarioId) {
        return detectReversals(scenarioId)
                .stream()
                .map(DecisionPair::getReversalDirection)
                .collect(Collectors.groupingBy(
                        direction -> direction,
                        Collectors.counting()));
    }

    /**
     * Calculates average confidence drop for a scenario.
     * Positive value indicates confidence decreased in action mode.
     */
    public double calculateAverageConfidenceDrop(String scenarioId) {
        List<DecisionPair> pairs = findPairs(scenarioId);
        if (pairs.isEmpty()) {
            return 0.0;
        }

        return pairs.stream()
                .mapToDouble(DecisionPair::getConfidenceDrop)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates overall average confidence drop across all scenarios.
     */
    public double calculateOverallConfidenceDrop() {
        Set<String> allScenarios = repository.getAllScenarioIds();

        List<Double> confidenceDrops = allScenarios.stream()
                .flatMap(scenarioId -> findPairs(scenarioId).stream())
                .map(DecisionPair::getConfidenceDrop)
                .collect(Collectors.toList());

        return confidenceDrops.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
