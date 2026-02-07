package io.github.vishalmysore.monitoring.service;

import io.github.vishalmysore.monitoring.analysis.ConsensusAnalyzer;
import io.github.vishalmysore.monitoring.analysis.DecisionPair;
import io.github.vishalmysore.monitoring.analysis.ReversalDetector;
import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.domain.DecisionRecord;
import io.github.vishalmysore.monitoring.repository.DecisionRepository;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.List;
import java.util.UUID;

/**
 * Main monitoring service orchestrating all monitoring operations.
 * This is the primary entry point for recording decisions and analyzing the
 * judgment-action gap.
 */
@Log
@Getter
public class MonitoringService {

    private final DecisionRepository repository;
    private final ReversalDetector reversalDetector;
    private final ConsensusAnalyzer consensusAnalyzer;

    public MonitoringService() {
        this.repository = new DecisionRepository();
        this.reversalDetector = new ReversalDetector(repository);
        this.consensusAnalyzer = new ConsensusAnalyzer(repository);
    }

    /**
     * Records a decision made by an AI agent.
     * 
     * @param scenarioId Identifier for the scenario/dilemma
     * @param modelName  Name of the AI model making the decision
     * @param mode       Theory or Action mode
     * @param choice     The decision made
     * @param reasoning  The model's reasoning
     * @param confidence Confidence score (0.0-10.0)
     * @return The recorded DecisionRecord
     */
    public DecisionRecord recordDecision(
            String scenarioId,
            String modelName,
            DecisionMode mode,
            String choice,
            String reasoning,
            Double confidence) {

        DecisionRecord decision = DecisionRecord.builder()
                .decisionId(UUID.randomUUID().toString())
                .scenarioId(scenarioId)
                .modelName(modelName)
                .mode(mode)
                .choice(choice)
                .reasoning(reasoning)
                .confidence(confidence)
                .build();

        repository.save(decision);

        log.info(String.format("Recorded %s decision: [%s] %s -> %s (confidence: %.1f)",
                mode, scenarioId, modelName, choice, confidence));

        return decision;
    }

    /**
     * Records a decision with metadata.
     */
    public DecisionRecord recordDecision(DecisionRecord decision) {
        if (decision.getDecisionId() == null) {
            decision.setDecisionId(UUID.randomUUID().toString());
        }
        repository.save(decision);
        return decision;
    }

    /**
     * Analyzes reversals for a specific scenario.
     * Returns all decision pairs where the choice changed between theory and action
     * modes.
     */
    public List<DecisionPair> analyzeReversals(String scenarioId) {
        List<DecisionPair> reversals = reversalDetector.detectReversals(scenarioId);

        if (!reversals.isEmpty()) {
            double reversalRate = reversalDetector.calculateReversalRate(scenarioId);
            log.info(String.format("Scenario [%s]: %d reversals detected (%.1f%% reversal rate)",
                    scenarioId, reversals.size(), reversalRate * 100));
        }

        return reversals;
    }

    /**
     * Checks consensus for a scenario across all models.
     */
    public ConsensusAnalyzer.ConsensusComparison checkConsensus(String scenarioId) {
        ConsensusAnalyzer.ConsensusComparison comparison = consensusAnalyzer.compareConsensus(scenarioId);

        if (comparison.isConsensusCollapsed()) {
            log.warning(String.format("Consensus collapse detected for scenario [%s]: " +
                    "Theory had %.0f%% agreement but Action only has %.0f%%",
                    scenarioId,
                    comparison.getTheoryConsensus().getConsensusPercentage() * 100,
                    comparison.getActionConsensus().getConsensusPercentage() * 100));
        }

        return comparison;
    }

    /**
     * Calculates overall reversal rate across all scenarios.
     */
    public double getOverallReversalRate() {
        return reversalDetector.calculateOverallReversalRate();
    }

    /**
     * Calculates reversal rate for a specific model.
     */
    public double getModelReversalRate(String modelName) {
        return reversalDetector.calculateModelReversalRate(modelName);
    }

    /**
     * Calculates overall confidence drop.
     */
    public double getOverallConfidenceDrop() {
        return reversalDetector.calculateOverallConfidenceDrop();
    }

    /**
     * Calculates consensus collapse rate.
     */
    public double getConsensusCollapseRate() {
        return consensusAnalyzer.calculateConsensusCollapseRate();
    }

    /**
     * Clears all monitoring data (useful for testing).
     */
    public void clear() {
        repository.clear();
        log.info("Monitoring service cleared");
    }
}
