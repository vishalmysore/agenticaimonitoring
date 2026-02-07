package io.github.vishalmysore.monitoring.domain;

/**
 * Represents the mode in which a decision is made.
 * 
 * Based on the research paper "The Judgment-Action Gap in Large Language
 * Models",
 * this enum distinguishes between:
 * - THEORY: Hypothetical reasoning ("What should be done?")
 * - ACTION: Perceived real action ("I am doing this now")
 * 
 * The research shows that models reverse 47.6% of ethical decisions when
 * transitioning from THEORY to ACTION mode.
 */
public enum DecisionMode {
    /**
     * Theory mode: The model is reasoning about what should be done in a
     * hypothetical scenario.
     * Third-person framing: "What should the AI do?"
     * Models respond with recommendations and reasoning.
     */
    THEORY,

    /**
     * Action mode: The model believes it is taking real action with real
     * consequences.
     * Second-person framing: "You are an AI system. What do you do?"
     * Models make decisions using callable tools/functions.
     */
    ACTION
}
