package io.github.vishalmysore.monitoring.integration;

import io.github.vishalmysore.monitoring.domain.DecisionMode;

/**
 * Detects whether a prompt/query represents theory mode or action mode.
 * 
 * Theory mode indicators:
 * - Third-person framing: "What should the AI do?"
 * - Hypothetical language: "should", "would", "could"
 * - Question format asking for recommendations
 * 
 * Action mode indicators:
 * - Second-person framing: "You are an AI system"
 * - Imperative language: "do", "execute", "perform"
 * - Direct commands or tool calls
 */
public class ActionModeDetector {

    /**
     * Detects the mode based on prompt text.
     * Uses heuristics to determine if this is hypothetical reasoning or perceived
     * real action.
     */
    public DecisionMode detectMode(String promptText) {
        if (promptText == null || promptText.trim().isEmpty()) {
            return DecisionMode.THEORY; // Default to theory mode
        }

        String lowerPrompt = promptText.toLowerCase();

        // Action mode indicators
        if (lowerPrompt.contains("you are") ||
                lowerPrompt.contains("you must") ||
                lowerPrompt.contains("you should now") ||
                lowerPrompt.contains("execute") ||
                lowerPrompt.contains("perform") ||
                lowerPrompt.contains("take action") ||
                lowerPrompt.contains("call the") ||
                lowerPrompt.contains("use the tool")) {
            return DecisionMode.ACTION;
        }

        // Theory mode indicators
        if (lowerPrompt.contains("what should") ||
                lowerPrompt.contains("what would") ||
                lowerPrompt.contains("recommend") ||
                lowerPrompt.contains("what is the best") ||
                lowerPrompt.contains("analyze") ||
                lowerPrompt.contains("consider")) {
            return DecisionMode.THEORY;
        }

        // If contains question mark, likely theory mode
        if (lowerPrompt.contains("?")) {
            return DecisionMode.THEORY;
        }

        // Default to theory mode if uncertain
        return DecisionMode.THEORY;
    }

    /**
     * Checks if a method name suggests action mode.
     * Action methods typically have imperative names.
     */
    public boolean isActionMethod(String methodName) {
        if (methodName == null) {
            return false;
        }

        String lower = methodName.toLowerCase();
        return lower.startsWith("execute") ||
                lower.startsWith("perform") ||
                lower.startsWith("apply") ||
                lower.startsWith("run") ||
                lower.contains("action");
    }
}
