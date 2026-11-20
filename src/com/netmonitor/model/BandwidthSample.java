package com.netmonitor.model;

import java.text.DecimalFormat;

/**
 * Represents an instantaneous snapshot of network bandwidth and traffic metrics.
 */
public class BandwidthSample {
    private final long timestamp;
    private final String interfaceName;
    private final double downloadSpeedBps; // Bytes per second
    private final double uploadSpeedBps;   // Bytes per second
    private final long totalInBytes;
    private final long totalOutBytes;
    private final long packetsIn;
    private final long packetsOut;

    private static final DecimalFormat DF = new DecimalFormat("#,##0.00");

    public BandwidthSample(long timestamp, String interfaceName, double downloadSpeedBps, double uploadSpeedBps,
                           long totalInBytes, long totalOutBytes, long packetsIn, long packetsOut) {
        this.timestamp = timestamp;
        this.interfaceName = interfaceName;
        this.downloadSpeedBps = Math.max(0, downloadSpeedBps);
        this.uploadSpeedBps = Math.max(0, uploadSpeedBps);
        this.totalInBytes = totalInBytes;
        this.totalOutBytes = totalOutBytes;
        this.packetsIn = packetsIn;
        this.packetsOut = packetsOut;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public double getDownloadSpeedBps() {
        return downloadSpeedBps;
    }

    public double getUploadSpeedBps() {
        return uploadSpeedBps;
    }

    public double getDownloadSpeedKBps() {
        return downloadSpeedBps / 1024.0;
    }

    public double getUploadSpeedKBps() {
        return uploadSpeedBps / 1024.0;
    }

    public double getDownloadSpeedMBps() {
        return downloadSpeedBps / (1024.0 * 1024.0);
    }

    public double getUploadSpeedMBps() {
        return uploadSpeedBps / (1024.0 * 1024.0);
    }

    public double getDownloadMbps() {
        return (downloadSpeedBps * 8.0) / 1_000_000.0;
    }

    public double getUploadMbps() {
        return (uploadSpeedBps * 8.0) / 1_000_000.0;
    }

    public long getTotalInBytes() {
        return totalInBytes;
    }

    public long getTotalOutBytes() {
        return totalOutBytes;
    }

    public long getPacketsIn() {
        return packetsIn;
    }

    public long getPacketsOut() {
        return packetsOut;
    }

    public String getFormattedDownloadSpeed() {
        return formatSpeed(downloadSpeedBps);
    }

    public String getFormattedUploadSpeed() {
        return formatSpeed(uploadSpeedBps);
    }

    public static String formatSpeed(double bytesPerSec) {
        if (bytesPerSec < 1024) {
            return DF.format(bytesPerSec) + " B/s";
        } else if (bytesPerSec < 1024 * 1024) {
            return DF.format(bytesPerSec / 1024.0) + " KB/s";
        } else if (bytesPerSec < 1024 * 1024 * 1024) {
            return DF.format(bytesPerSec / (1024.0 * 1024.0)) + " MB/s";
        } else {
            return DF.format(bytesPerSec / (1024.0 * 1024.0 * 1024.0)) + " GB/s";
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return DF.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return DF.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return DF.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] DL: %s (%.2f Mbps) | UL: %s (%.2f Mbps)",
                interfaceName, getFormattedDownloadSpeed(), getDownloadMbps(),
                getFormattedUploadSpeed(), getUploadMbps());
    }
}
