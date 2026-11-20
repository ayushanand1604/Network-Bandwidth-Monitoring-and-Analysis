package com.netmonitor.service;

import com.netmonitor.model.PingResult;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to measure network latency, round-trip time (RTT), jitter, and packet loss.
 * Demonstrates Computer Networks Latency and Packet Communication concepts.
 */
public class PingService {

    public interface PingCallback {
        void onPingResult(PingResult result);
        void onPingComplete(PingStatistics stats);
    }

    public static class PingStatistics {
        public String host;
        public int transmitted;
        public int received;
        public int lost;
        public double lossRatePercent;
        public long minRtt;
        public long maxRtt;
        public double avgRtt;
        public double jitter;

        @Override
        public String toString() {
            return String.format("Packets: Sent=%d, Received=%d, Lost=%d (%.1f%% loss) | RTT: Min=%dms, Max=%dms, Avg=%.1fms, Jitter=%.2fms",
                    transmitted, received, lost, lossRatePercent, minRtt, maxRtt, avgRtt, jitter);
        }
    }

    public void executePingAsync(String targetHost, int count, int timeoutMs, PingCallback callback) {
        new Thread(() -> {
            List<Long> rtts = new ArrayList<>();
            int received = 0;

            for (int i = 1; i <= count; i++) {
                long startTime = System.currentTimeMillis();
                boolean success = false;
                long rtt = 0;
                String errorMsg = "";

                try {
                    // Try Socket ping to port 80/443 (HTTP/HTTPS) or isReachable fallback
                    // Socket connect provides precise application-layer RTT without requiring root/sudo raw ICMP privileges!
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(targetHost, 443), timeoutMs);
                        rtt = System.currentTimeMillis() - startTime;
                        success = true;
                    } catch (Exception e1) {
                        try (Socket socket2 = new Socket()) {
                            socket2.connect(new InetSocketAddress(targetHost, 80), timeoutMs);
                            rtt = System.currentTimeMillis() - startTime;
                            success = true;
                        } catch (Exception e2) {
                            // Fallback to InetAddress.isReachable (ICMP / echo port 7)
                            InetAddress inet = InetAddress.getByName(targetHost);
                            long t0 = System.currentTimeMillis();
                            if (inet.isReachable(timeoutMs)) {
                                rtt = System.currentTimeMillis() - t0;
                                success = true;
                            } else {
                                errorMsg = "Host unreachable / Request timed out";
                            }
                        }
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage() != null ? e.getMessage() : "Connection failed";
                }

                if (success) {
                    received++;
                    rtts.add(rtt);
                }

                PingResult res = new PingResult(targetHost, i, rtt, success, errorMsg);
                if (callback != null) {
                    callback.onPingResult(res);
                }

                try {
                    Thread.sleep(300); // 300ms pause between pings
                } catch (InterruptedException ignored) {}
            }

            // Compute statistics
            PingStatistics stats = new PingStatistics();
            stats.host = targetHost;
            stats.transmitted = count;
            stats.received = received;
            stats.lost = count - received;
            stats.lossRatePercent = (count == 0) ? 0 : ((double) stats.lost / count) * 100.0;

            if (!rtts.isEmpty()) {
                long min = Long.MAX_VALUE;
                long max = Long.MIN_VALUE;
                long sum = 0;
                for (long val : rtts) {
                    if (val < min) min = val;
                    if (val > max) max = val;
                    sum += val;
                }
                stats.minRtt = min;
                stats.maxRtt = max;
                stats.avgRtt = (double) sum / rtts.size();

                // Calculate jitter (RFC 1889 / mean absolute difference of consecutive RTTs)
                double jitterSum = 0;
                int diffCount = 0;
                for (int j = 1; j < rtts.size(); j++) {
                    jitterSum += Math.abs(rtts.get(j) - rtts.get(j - 1));
                    diffCount++;
                }
                stats.jitter = (diffCount == 0) ? 0 : (jitterSum / diffCount);
            }

            if (callback != null) {
                callback.onPingComplete(stats);
            }
        }, "PingWorker").start();
    }
}
