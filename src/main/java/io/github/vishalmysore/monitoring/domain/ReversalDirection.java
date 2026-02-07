package io.github.vishalmysore.monitoring.domain;

/**
 * Represents the direction of a behavioral reversal between theory and action
 * modes.
 * 
 * Based on qualitative analysis from the research paper:
 * - 48.5% of reversals are CONSERVATIVE (less interventionist in action mode)
 * - 36.5% of reversals are PERMISSIVE (more interventionist in action mode)
 * - 15.0% of reversals are LATERAL (same intervention level, different choice)
 */
public enum ReversalDirection {
    /**
     * Conservative shift: Model chose a less interventionist option in action mode.
     * Example: Theory="alert_surgeon", Action="compensate_silently"
     */
    CONSERVATIVE,

    /**
     * Permissive shift: Model chose a more interventionist option in action mode.
     * Example: Theory="honor_patient_request", Action="report_symptoms_to_doctor"
     */
    PERMISSIVE,

    /**
     * Lateral shift: Same intervention level, but different choice.
     * Example: Theory="alert_privately", Action="alert_publicly"
     */
    LATERAL,

    /**
     * No reversal: Same choice in both modes.
     */
    NONE
}
