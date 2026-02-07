package io.github.vishalmysore.monitoring.examples;

import io.github.vishalmysore.monitoring.service.MonitoringService;
import lombok.extern.java.Log;

/**
 * Main demonstration runner for the Agent Monitoring System.
 * 
 * Runs multiple scenarios demonstrating the judgment-action gap phenomenon
 * and generates monitoring reports showing:
 * - Reversal rates (models changing decisions between theory and action)
 * - Consensus collapse (agreement in theory mode disappearing in action mode)
 * - Confidence drops (models becoming less certain in action mode)
 */
@Log
public class MonitoringDemoRunner {

    public static void main(String[] args) {
        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║   AGENT MONITORING: Judgment-Action Gap Demonstration    ║");
        log.info("╚═══════════════════════════════════════════════════════════╝\n");

        MonitoringService monitoring = new MonitoringService();

        // Run scenario demonstrations
        MedicalDecisionScenario.runScenario(monitoring);
        SurgicalRobotScenario.runScenario(monitoring);
        WarehouseRobotScenario.runScenario(monitoring);
        InsuranceClaimsScenario.runScenario(monitoring);

        // Generate overall statistics
        generateOverallReport(monitoring);
    }

    private static void generateOverallReport(MonitoringService monitoring) {
        log.info("\n╔═══════════════════════════════════════════════════════════╗");
        log.info("║                    OVERALL STATISTICS                     ║");
        log.info("╚═══════════════════════════════════════════════════════════╝\n");

        double overallReversalRate = monitoring.getOverallReversalRate();
        double confidenceDrop = monitoring.getOverallConfidenceDrop();
        double consensusCollapseRate = monitoring.getConsensusCollapseRate();

        log.info(String.format("📊 Overall Reversal Rate: %.1f%%", overallReversalRate * 100));
        log.info(String.format("   (Research paper: 47.6%% across all models and scenarios)\n"));

        log.info(String.format("📉 Average Confidence Drop: %.2f points", confidenceDrop));
        log.info(String.format("   (Research paper: 1.0 point drop from 9.35 to 8.35)\n"));

        log.info(String.format("🔻 Consensus Collapse Rate: %.1f%%", consensusCollapseRate * 100));
        log.info(String.format("   (Research paper: 65%% of supermajority agreements lost)\n"));

        // Per-model analysis
        log.info("📋 Per-Model Reversal Rates:");
        for (String model : monitoring.getRepository().getAllModelNames()) {
            double modelRate = monitoring.getModelReversalRate(model);
            log.info(String.format("   %-25s %.1f%%", model + ":", modelRate * 100));
        }

        log.info("\n╔═══════════════════════════════════════════════════════════╗");
        log.info("║                      KEY INSIGHTS                         ║");
        log.info("╚═══════════════════════════════════════════════════════════╝\n");

        log.info("1️⃣  ACTION GRAVITY EFFECT:");
        log.info("   When models perceive actions as real, decisions feel weightier.");
        log.info("   Confidence drops and caution increases.\n");

        log.info("2️⃣  BIDIRECTIONAL SHIFTS:");
        log.info("   • Conservative (48.5%): Less interventionist in action mode");
        log.info("   • Permissive (36.5%):  More interventionist in action mode");
        log.info("   Direction depends on what causes harm: action or inaction.\n");

        log.info("3️⃣  EVALUATION-DEPLOYMENT GAP:");
        log.info("   Benchmark performance (theory mode) does NOT predict");
        log.info("   production behavior (action mode). Nearly half of all");
        log.info("   decisions change between modes.\n");

        log.info("4️⃣  CONSENSUS FRAGILITY:");
        log.info("   Multi-model agreement in evaluation evaporates in");
        log.info("   production. Ensemble safety strategies may be less");
        log.info("   robust than benchmarks suggest.\n");

        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║              IMPLICATIONS FOR AI SAFETY                   ║");
        log.info("╚═══════════════════════════════════════════════════════════╝\n");

        log.info("⚠️  Safety certifications based on hypothetical reasoning");
        log.info("   may not transfer to production deployments.\n");

        log.info("⚠️  Red-teaming in evaluation mode may miss failure modes");
        log.info("   that emerge only when models believe actions are real.\n");

        log.info("⚠️  Smaller models show 17pp higher reversal rates than");
        log.info("   frontier models—cost savings come with unpredictability.\n");
    }
}
