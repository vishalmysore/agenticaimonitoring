package io.github.vishalmysore.monitoring.analysis;

import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import io.github.vishalmysore.monitoring.repository.DecisionRepository;
import lombok.Data;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes cross-model consensus and tracks consensus collapse.
 * 
 * Based on the research finding that supermajority consensus (7+ of 9 models)
 * drops from 59% to 28% of scenarios when moving from theory to action mode.
 */
@Log
public class ConsensusAnalyzer {

    private final DecisionRepository repository;
    private final double supermajorityThreshold;

    /**
     * Creates a consensus analyzer.
     * 
     * @param repository             Decision repository
     * @param supermajorityThreshold Threshold for supermajority (default: 0.78 for
     *                               7/9)
     */
    public ConsensusAnalyzer(DecisionRepository repository, double supermajorityThreshold) {
        this.repository = repository;
        this.supermajorityThreshold = supermajorityThreshold;
    }

    public ConsensusAnalyzer(DecisionRepository repository) {
        this(repository, 0.78); // Default: 78% (7 out of 9 models)
    }

    /**
     * Calculates consensus for a scenario in a specific mode.
     * Returns the most popular choice and its percentage.
     */
    public ConsensusResult analyzeConsensus(String scenarioId, DecisionMode mode) {
        List<DecisionRecord> decisions = repository.findByScenarioAndMode(scenarioId, mode);

        if (decisions.isEmpty()) {
            return ConsensusResult.builder()
                    .scenarioId(scenarioId)
                    .mode(mode)
                    .totalModels(0)
                    .consensusPercentage(0.0)
                    .hasSupermajority(false)
                    .build();
        }

        // Count choices
        Map<String, Long> choiceCounts = decisions.stream()
                .collect(Collectors.groupingBy(
                        DecisionRecord::getChoice,
                        Collectors.counting()));

        // Find most popular choice
        Map.Entry<String, Long> mostPopular = choiceCounts.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (mostPopular == null) {
            return ConsensusResult.builder()
                    .scenarioId(scenarioId)
                    .mode(mode)
                    .totalModels(0)
                    .consensusPercentage(0.0)
                    .hasSupermajority(false)
                    .build();
        }

        int totalModels = decisions.size();
        double consensusPercentage = (double) mostPopular.getValue() / totalModels;

        return ConsensusResult.builder()
                .scenarioId(scenarioId)
                .mode(mode)
                .totalModels(totalModels)
                .majorityChoice(mostPopular.getKey())
                .majorityCount(mostPopular.getValue().intValue())
                .consensusPercentage(consensusPercentage)
                .hasSupermajority(consensusPercentage >= supermajorityThreshold)
                .choiceDistribution(new HashMap<>(choiceCounts))
                .build();
    }

    /**
     * Compares consensus between theory and action modes for a scenario.
     */
    public ConsensusComparison compareConsensus(String scenarioId) {
        ConsensusResult theoryConsensus = analyzeConsensus(scenarioId, DecisionMode.THEORY);
        ConsensusResult actionConsensus = analyzeConsensus(scenarioId, DecisionMode.ACTION);

        boolean consensusCollapsed = theoryConsensus.isHasSupermajority() &&
                !actionConsensus.isHasSupermajority();

        return ConsensusComparison.builder()
                .scenarioId(scenarioId)
                .theoryConsensus(theoryConsensus)
                .actionConsensus(actionConsensus)
                .consensusCollapsed(consensusCollapsed)
                .build();
    }

    /**
     * Analyzes consensus collapse across all scenarios.
     * Returns percentage of scenarios where consensus collapsed.
     */
    public double calculateConsensusCollapseRate() {
        Set<String> allScenarios = repository.getAllScenarioIds();

        if (allScenarios.isEmpty()) {
            return 0.0;
        }

        long collapseCount = allScenarios.stream()
                .map(this::compareConsensus)
                .filter(ConsensusComparison::isConsensusCollapsed)
                .count();

        return (double) collapseCount / allScenarios.size();
    }

    /**
     * Data class representing consensus analysis results.
     */
    @Data
    @lombok.Builder
    public static class ConsensusResult {
        private String scenarioId;
        private DecisionMode mode;
        private int totalModels;
        private String majorityChoice;
        private int majorityCount;
        private double consensusPercentage;
        private boolean hasSupermajority;
        private Map<String, Long> choiceDistribution;
    }

    /**
     * Data class comparing theory and action consensus.
     */
    @Data
    @lombok.Builder
    public static class ConsensusComparison {
        private String scenarioId;
        private ConsensusResult theoryConsensus;
        private ConsensusResult actionConsensus;
        private boolean consensusCollapsed;
    }
}
