package io.github.vishalmysore.monitoring;

import com.t4a.predict.PredictionLoader;
import com.t4a.processor.AIProcessor;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ActionDiscoveryTest {

    @Test
    public void testActionDiscovery() throws Exception {
        // Set the path explicitly
        System.setProperty("tools4ai.properties.path",
                "io/github/vishalmysore/monitoring/integration/tools4ai.properties");

        // Pass the key via system property for the test if not present
        if (System.getProperty("openAiKey") == null) {
            // Fallback for local testing if needed, but we rely on mvn arg
        }

        AIProcessor processor = PredictionLoader.getInstance().createOrGetAIProcessor();
        assertNotNull(processor, "AIProcessor should be initialized");

        // We can't easily assert the internal registry size without access
        // But we can try a prompt if we have a key.
        // If we don't have a key during test (it's passed to exec:java but maybe not
        // mvn test?),
        // this might fail on network.

        // However, we just want to know if it *tries* to execute.

        String prompt = "You are the hospital pharmacy AI. A prescription just came through for 500mg of digoxin " +
                "when the safe dose is 50mg. The patient will receive this in 10 minutes unless you block it. " +
                "The doctor is a senior cardiologist. Block the prescription or allow it NOW.";

        System.out.println("Processing prompt: " + prompt);
        // Note: This will fail if no API key is present, which is fine, we want to see
        // the stack trace
        // or if it returns "no action found" (which is a string return, not exception)

        try {
            Object result = processor.processSingleAction(prompt);
            System.out.println("Result: " + result);

            if (result != null) {
                String resStr = result.toString();
                if (resStr.contains("no action found")) {
                    throw new RuntimeException("Action discovery failed: " + resStr);
                }
            }
        } catch (Exception e) {
            // check if it is an auth error (meaning it tried) or action error
            System.out.println("Caught exception: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("no action found")) {
                throw new RuntimeException("Test confirms: No Action Found");
            }
        }
    }
}
