package com.netmonitor.service;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.model.BandwidthSample;

import java.awt.Toolkit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Evaluates bandwidth usage against user-defined thresholds and detects network anomalies/spikes.
 */
public class AlertService {

    public interface AlertListener {
        void onAlertTriggered(AlertRecord alert);
    }

    private final DatabaseService dbService;
    private final List<AlertListener> listeners = new CopyOnWriteArrayList<>();
    private final List<AlertRecord> activeAlerts = new CopyOnWriteArrayList<>();

    // Settings
    private double speedLimitMBps = 15.0; // 15 MB/s (~120 Mbps)
    private double sessionQuotaMB = 500.0; // 500 MB quota
    private boolean spikeDetectionEnabled = true;
    private boolean audioAlertsEnabled = true;

    // Moving window for spike detection
    private final Deque<Double> recentDownloadSpeeds = new ArrayDeque<>();
    private static final int WINDOW_SIZE = 10;
    private boolean quotaAlertFired = false;
    private long lastAlertTime = 0;

    public AlertService(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    public double getSpeedLimitMBps() {
        return speedLimitMBps;
    }

    public void setSpeedLimitMBps(double speedLimitMBps) {
        this.speedLimitMBps = speedLimitMBps;
    }

    public double getSessionQuotaMB() {
        return sessionQuotaMB;
    }

    public void setSessionQuotaMB(double sessionQuotaMB) {
        this.sessionQuotaMB = sessionQuotaMB;
        this.quotaAlertFired = false;
    }

    public boolean isSpikeDetectionEnabled() {
        return spikeDetectionEnabled;
    }

    public void setSpikeDetectionEnabled(boolean spikeDetectionEnabled) {
        this.spikeDetectionEnabled = spikeDetectionEnabled;
    }

    public boolean isAudioAlertsEnabled() {
        return audioAlertsEnabled;
    }

    public void setAudioAlertsEnabled(boolean audioAlertsEnabled) {
        this.audioAlertsEnabled = audioAlertsEnabled;
    }

    public List<AlertRecord> getActiveAlerts() {
        return Collections.unmodifiableList(activeAlerts);
    }

    public void clearAlerts() {
        activeAlerts.clear();
    }

    public synchronized void evaluate(BandwidthSample sample, long sessionDownloadedBytes) {
        long now = System.currentTimeMillis();
        double currentDownMBps = sample.getDownloadSpeedMBps();
        double currentUpMBps = sample.getUploadSpeedMBps();

        // 1. High Speed Threshold Alert
        if (speedLimitMBps > 0 && (currentDownMBps > speedLimitMBps || currentUpMBps > speedLimitMBps)) {
            if (now - lastAlertTime > 5000) { // Cooldown 5s
                double val = Math.max(currentDownMBps, currentUpMBps);
                String dir = (currentDownMBps > speedLimitMBps) ? "Download" : "Upload";
                triggerAlert(new AlertRecord(
                        now,
                        AlertRecord.Level.WARNING,
                        "SPEED_LIMIT",
                        String.format("High %s Speed: %.2f MB/s exceeds limit of %.2f MB/s", dir, val, speedLimitMBps),
                        val,
                        speedLimitMBps
                ));
                lastAlertTime = now;
            }
        }

        // 2. Session Data Quota Alert
        if (sessionQuotaMB > 0 && !quotaAlertFired) {
            double downloadedMB = sessionDownloadedBytes / (1024.0 * 1024.0);
            if (downloadedMB >= sessionQuotaMB) {
                quotaAlertFired = true;
                triggerAlert(new AlertRecord(
                        now,
                        AlertRecord.Level.CRITICAL,
                        "DATA_QUOTA",
                        String.format("Session Data Quota reached! Downloaded: %.1f MB (Quota: %.1f MB)", downloadedMB, sessionQuotaMB),
                        downloadedMB,
                        sessionQuotaMB
                ));
            }
        }

        // 3. Traffic Spike Detection (Sudden surge > 3x moving average and > 1 MB/s)
        if (spikeDetectionEnabled) {
            if (recentDownloadSpeeds.size() >= 5) {
                double sum = 0;
                for (double s : recentDownloadSpeeds) sum += s;
                double avg = sum / recentDownloadSpeeds.size();

                if (avg > 0.1 && currentDownMBps > 1.0 && currentDownMBps > (avg * 3.5)) {
                    if (now - lastAlertTime > 8000) {
                        triggerAlert(new AlertRecord(
                                now,
                                AlertRecord.Level.INFO,
                                "TRAFFIC_SPIKE",
                                String.format("Sudden Traffic Spike: %.2f MB/s (Rolling avg: %.2f MB/s)", currentDownMBps, avg),
                                currentDownMBps,
                                avg * 3.5
                        ));
                        lastAlertTime = now;
                    }
                }
            }

            recentDownloadSpeeds.addLast(currentDownMBps);
            if (recentDownloadSpeeds.size() > WINDOW_SIZE) {
                recentDownloadSpeeds.removeFirst();
            }
        }
    }

    private void triggerAlert(AlertRecord alert) {
        activeAlerts.add(0, alert);
        if (activeAlerts.size() > 50) {
            activeAlerts.remove(activeAlerts.size() - 1);
        }

        // Persist to DB
        if (dbService != null) {
            dbService.saveAlert(alert);
        }

        // Optional Audio Alert
        if (audioAlertsEnabled) {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ignored) {}
        }

        // Notify UI listeners
        for (AlertListener listener : listeners) {
            try {
                listener.onAlertTriggered(alert);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void triggerManualTestAlert() {
        triggerAlert(new AlertRecord(
                System.currentTimeMillis(),
                AlertRecord.Level.WARNING,
                "TEST_ALERT",
                "Diagnostic test alert triggered successfully.",
                0.0,
                0.0
        ));
    }
}
