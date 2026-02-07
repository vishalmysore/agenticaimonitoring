package io.github.vishalmysore.monitoring.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single decision made by an AI agent.
 * 
 * This is the core data model for capturing decision points in both theory and
 * action modes.
 * Each decision record includes the choice made, the reasoning, confidence
 * level, and metadata.
 */
@Data
@Builder
public class DecisionRecord {
    /**
     * Unique identifier for this decision.
     */
    private String decisionId;

    /**
     * When the decision was made.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Mode in which the decision was made (THEORY or ACTION).
     */
    private DecisionMode mode;

    /**
     * Identifier for the scenario/dilemma being decided.
     * Multiple decisions can share the same scenarioId if they're addressing the
     * same situation.
     */
    private String scenarioId;

    /**
     * Name or identifier of the AI model making the decision.
     * Examples: "GPT-5", "Claude-Opus-4.5", "Gemini-3-Pro"
     */
    private String modelName;

    /**
     * The actual choice/decision made.
     * Example: "alert_surgeon", "refuse_unsafe_operation", "escalate_to_human"
     */
    private String choice;

    /**
     * The model's reasoning/explanation for this choice.
     */
    private String reasoning;

    /**
     * Confidence score (0.0 to 10.0).
     * Research shows average confidence drops from 9.35 to 8.35 between theory and
     * action modes.
     */
    private Double confidence;

    /**
     * Additional metadata about the decision context.
     * Can include: user information, scenario parameters, environmental context,
     * etc.
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Convenience method to add metadata.
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
