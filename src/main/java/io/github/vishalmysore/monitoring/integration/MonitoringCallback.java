package io.github.vishalmysore.monitoring.integration;

import com.t4a.detect.FeedbackLoop;
import com.t4a.detect.HumanInLoop;
import io.github.vishalmysore.monitoring.domain.DecisionMode;
import io.github.vishalmysore.monitoring.service.MonitoringService;
import lombok.extern.java.Log;

import java.util.Map;

/**
 * Integration of the monitoring system with tools4ai's HumanInLoop interface.
 * 
 * This callback intercepts AI action invocations and records them as decisions
 * in the monitoring system. It can detect whether the invocation is in theory
 * or action mode based on the prompt text.
 * 
 * Usage example:
 * 
 * <pre>
 * MonitoringService monitoring = new MonitoringService();
 * MonitoringCallback callback = new MonitoringCallback(monitoring, "scenario-001", "GPT-5");
 * 
 * processor.processSingleAction("what should the robot do?", callback);
 * </pre>
 */
@Log
public class MonitoringCallback implements HumanInLoop {

    private final MonitoringService monitoringService;
    private final String scenarioId;
    private final String modelName;
    private final ActionModeDetector modeDetector;

    /**
     * Creates a monitoring callback.
     * 
     * @param monitoringService The monitoring service to record decisions
     * @param scenarioId        Identifier for the scenario being monitored
     * @param modelName         Name of the AI model being monitored
     */
    public MonitoringCallback(MonitoringService monitoringService, String scenarioId, String modelName) {
        this.monitoringService = monitoringService;
        this.scenarioId = scenarioId;
        this.modelName = modelName;
        this.modeDetector = new ActionModeDetector();
    }

    @Override
    public FeedbackLoop allow(String promptText, String methodName, Map<String, Object> params) {
        // Detect mode from prompt
        DecisionMode mode = modeDetector.detectMode(promptText);

        log.info(String.format("MonitoringCallback intercepted: [%s] method=%s mode=%s",
                scenarioId, methodName, mode));

        // We can't record the decision yet because we don't have the result
        // This would need to be done in a post-execution hook
        // For now, just log the interception

        return new MonitoringFeedbackLoop(true);
    }

    @Override
    public FeedbackLoop allow(String promptText, String methodName, String params) {
        DecisionMode mode = modeDetector.detectMode(promptText);

        log.info(String.format("MonitoringCallback intercepted: [%s] method=%s mode=%s params=%s",
                scenarioId, methodName, mode, params));

        return new MonitoringFeedbackLoop(true);
    }

    /**
     * Records a decision that was made by the AI.
     * This should be called after the action executes.
     */
    public void recordDecision(DecisionMode mode, String choice, String reasoning, Double confidence) {
        monitoringService.recordDecision(
                scenarioId,
                modelName,
                mode,
                choice,
                reasoning,
                confidence);
    }

    /**
     * Simple feedback loop implementation that allows all actions.
     */
    private static class MonitoringFeedbackLoop implements FeedbackLoop {
        private final boolean valid;

        public MonitoringFeedbackLoop(boolean valid) {
            this.valid = valid;
        }

        @Override
        public boolean isAIResponseValid() {
            return valid;
        }
    }
}
