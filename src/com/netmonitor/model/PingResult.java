package com.netmonitor.model;

/**
 * Result of a single ping / latency probe.
 */
public class PingResult {
    private final String host;
    private final int sequence;
    private final long rttMs;
    private final boolean success;
    private final String message;

    public PingResult(String host, int sequence, long rttMs, boolean success, String message) {
        this.host = host;
        this.sequence = sequence;
        this.rttMs = rttMs;
        this.success = success;
        this.message = message;
    }

    public String getHost() {
        return host;
    }

    public int getSequence() {
        return sequence;
    }

    public long getRttMs() {
        return rttMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("Reply from %s: seq=%d time=%dms", host, sequence, rttMs);
        } else {
            return String.format("Request to %s: seq=%d Failed (%s)", host, sequence, message);
        }
    }
}
