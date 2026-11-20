package com.netmonitor.model;

/**
 * Summary metrics of a monitoring session.
 */
public class SessionSummary {
    private final long startTime;
    private long endTime;
    private long totalBytesIn;
    private long totalBytesOut;
    private double peakDownSpeedBps;
    private double peakUpSpeedBps;
    private double sumDownSpeedBps;
    private double sumUpSpeedBps;
    private int sampleCount;
    private long totalPacketsIn;
    private long totalPacketsOut;

    public SessionSummary(long startTime) {
        this.startTime = startTime;
        this.endTime = startTime;
    }

    public synchronized void addSample(BandwidthSample sample) {
        sampleCount++;
        endTime = sample.getTimestamp();
        totalBytesIn = sample.getTotalInBytes();
        totalBytesOut = sample.getTotalOutBytes();
        totalPacketsIn = sample.getPacketsIn();
        totalPacketsOut = sample.getPacketsOut();

        if (sample.getDownloadSpeedBps() > peakDownSpeedBps) {
            peakDownSpeedBps = sample.getDownloadSpeedBps();
        }
        if (sample.getUploadSpeedBps() > peakUpSpeedBps) {
            peakUpSpeedBps = sample.getUploadSpeedBps();
        }

        sumDownSpeedBps += sample.getDownloadSpeedBps();
        sumUpSpeedBps += sample.getUploadSpeedBps();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDurationSeconds() {
        return Math.max(1, (endTime - startTime) / 1000);
    }

    public long getTotalBytesIn() {
        return totalBytesIn;
    }

    public long getTotalBytesOut() {
        return totalBytesOut;
    }

    public double getPeakDownSpeedBps() {
        return peakDownSpeedBps;
    }

    public double getPeakUpSpeedBps() {
        return peakUpSpeedBps;
    }

    public double getAvgDownSpeedBps() {
        return sampleCount == 0 ? 0 : (sumDownSpeedBps / sampleCount);
    }

    public double getAvgUpSpeedBps() {
        return sampleCount == 0 ? 0 : (sumUpSpeedBps / sampleCount);
    }

    public long getTotalPacketsIn() {
        return totalPacketsIn;
    }

    public long getTotalPacketsOut() {
        return totalPacketsOut;
    }

    public int getSampleCount() {
        return sampleCount;
    }
}
