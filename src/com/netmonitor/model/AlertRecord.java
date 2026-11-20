package com.netmonitor.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model representing a network alert (threshold exceeded, quota reached, spike detected).
 */
public class AlertRecord {
    public enum Level {
        INFO, WARNING, CRITICAL
    }

    private final long timestamp;
    private final Level level;
    private final String ruleType;
    private final String message;
    private final double value;
    private final double threshold;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AlertRecord(long timestamp, Level level, String ruleType, String message, double value, double threshold) {
        this.timestamp = timestamp;
        this.level = level;
        this.ruleType = ruleType;
        this.message = message;
        this.value = value;
        this.threshold = threshold;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return SDF.format(new Date(timestamp));
    }

    public Level getLevel() {
        return level;
    }

    public String getRuleType() {
        return ruleType;
    }

    public String getMessage() {
        return message;
    }

    public double getValue() {
        return value;
    }

    public double getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return String.format("[%s] [%s] %s (Value: %.2f, Limit: %.2f)",
                getFormattedTimestamp(), level, message, value, threshold);
    }
}
