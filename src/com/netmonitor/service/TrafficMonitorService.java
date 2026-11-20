package com.netmonitor.service;

import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.SessionSummary;

import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core periodic monitoring service.
 * Schedules background polling of OS interface counters, computes instantaneous
 * upload/download speeds, notifies UI listeners, and feeds data to persistence and alert engines.
 */
public class TrafficMonitorService {

    public interface TrafficListener {
        void onTrafficSample(BandwidthSample sample);
    }

    private final SystemTrafficReader trafficReader;
    private final DatabaseService dbService;
    private final AlertService alertService;
    private final List<TrafficListener> listeners = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private String targetInterface = null; // null or "all" monitors aggregate

    private long lastTimestamp = 0;
    private long lastRxBytes = -1;
    private long lastTxBytes = -1;
    private long lastRxPackets = -1;
    private long lastTxPackets = -1;

    private SessionSummary currentSession;
    private int pollingIntervalMs = 1000;
    private int dbSaveCounter = 0;

    public TrafficMonitorService(SystemTrafficReader trafficReader, DatabaseService dbService, AlertService alertService) {
        this.trafficReader = trafficReader;
        this.dbService = dbService;
        this.alertService = alertService;
    }

    public void addListener(TrafficListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrafficListener listener) {
        listeners.remove(listener);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public String getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(String targetInterface) {
        this.targetInterface = targetInterface;
        resetCounters();
    }

    public int getPollingIntervalMs() {
        return pollingIntervalMs;
    }

    public void setPollingIntervalMs(int pollingIntervalMs) {
        this.pollingIntervalMs = Math.max(250, pollingIntervalMs);
        if (isRunning.get()) {
            stop();
            start();
        }
    }

    public SessionSummary getCurrentSession() {
        return currentSession;
    }

    public synchronized void start() {
        if (isRunning.get()) return;
        isRunning.set(true);
        resetCounters();
        currentSession = new SessionSummary(System.currentTimeMillis());

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TrafficMonitorThread");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::pollNetworkStats, 0, pollingIntervalMs, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (!isRunning.get()) return;
        isRunning.set(false);
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        // Save session summary to DB
        if (dbService != null && currentSession != null) {
            String ifName = (targetInterface != null) ? targetInterface : "default";
            dbService.saveSession(currentSession, ifName);
        }
    }

    private void resetCounters() {
        lastTimestamp = 0;
        lastRxBytes = -1;
        lastTxBytes = -1;
        lastRxPackets = -1;
        lastTxPackets = -1;
    }

    private void pollNetworkStats() {
        try {
            long now = System.currentTimeMillis();
            Map<String, SystemTrafficReader.InterfaceStats> allStats = trafficReader.readAllStats();

            long totalRx = 0;
            long totalTx = 0;
            long totalPktsRx = 0;
            long totalPktsTx = 0;
            String ifLabel;

            if (targetInterface != null && !targetInterface.equalsIgnoreCase("all") && allStats.containsKey(targetInterface)) {
                SystemTrafficReader.InterfaceStats s = allStats.get(targetInterface);
                totalRx = s.rxBytes;
                totalTx = s.txBytes;
                totalPktsRx = s.rxPackets;
                totalPktsTx = s.txPackets;
                ifLabel = targetInterface;
            } else {
                // If specific interface not selected, aggregate all active interfaces (or pick en0 / primary)
                if (allStats.containsKey("en0")) {
                    SystemTrafficReader.InterfaceStats s = allStats.get("en0");
                    totalRx = s.rxBytes;
                    totalTx = s.txBytes;
                    totalPktsRx = s.rxPackets;
                    totalPktsTx = s.txPackets;
                    ifLabel = "en0";
                } else if (!allStats.isEmpty()) {
                    // Sum all non-loopback or aggregate
                    for (Map.Entry<String, SystemTrafficReader.InterfaceStats> entry : allStats.entrySet()) {
                        if (!entry.getKey().startsWith("lo")) {
                            totalRx += entry.getValue().rxBytes;
                            totalTx += entry.getValue().txBytes;
                            totalPktsRx += entry.getValue().rxPackets;
                            totalPktsTx += entry.getValue().txPackets;
                        }
                    }
                    ifLabel = "Active Aggregate";
                } else {
                    ifLabel = "None";
                }
            }

            double downloadBps = 0;
            double uploadBps = 0;

            if (lastTimestamp > 0 && lastRxBytes >= 0 && lastTxBytes >= 0) {
                double elapsedSec = (now - lastTimestamp) / 1000.0;
                if (elapsedSec > 0) {
                    long deltaRx = Math.max(0, totalRx - lastRxBytes);
                    long deltaTx = Math.max(0, totalTx - lastTxBytes);
                    downloadBps = deltaRx / elapsedSec;
                    uploadBps = deltaTx / elapsedSec;
                }
            }

            lastTimestamp = now;
            lastRxBytes = totalRx;
            lastTxBytes = totalTx;
            lastRxPackets = totalPktsRx;
            lastTxPackets = totalPktsTx;

            BandwidthSample sample = new BandwidthSample(
                    now,
                    ifLabel,
                    downloadBps,
                    uploadBps,
                    totalRx,
                    totalTx,
                    totalPktsRx,
                    totalPktsTx
            );

            if (currentSession != null) {
                currentSession.addSample(sample);
            }

            // Alerts evaluation
            if (alertService != null && currentSession != null) {
                alertService.evaluate(sample, currentSession.getTotalBytesIn());
            }

            // Persist to SQLite periodically (every 2 samples)
            if (dbService != null) {
                dbSaveCounter++;
                if (dbSaveCounter >= 2) {
                    dbService.saveSample(sample);
                    dbSaveCounter = 0;
                }
            }

            // Dispatch to UI on Swing Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                for (TrafficListener listener : listeners) {
                    try {
                        listener.onTrafficSample(sample);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
