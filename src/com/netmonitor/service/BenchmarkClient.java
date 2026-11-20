package com.netmonitor.service;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Client for executing TCP/UDP throughput and packet benchmarks against a BenchmarkServer or local host.
 * Demonstrates:
 * - Throughput calculation: (Bytes Transferred * 8) / (Time in seconds * 10^6) Mbps
 * - TCP Stream Transmission
 * - UDP Datagram Packet Delivery & Packet Loss Analysis
 */
public class BenchmarkClient {

    public interface BenchmarkCallback {
        void onProgress(String phase, double currentMbps, int percentComplete);
        void onLog(String message);
        void onComplete(BenchmarkResult result);
        void onError(String errorMessage);
    }

    public static class BenchmarkResult {
        public double downloadSpeedMbps;
        public double uploadSpeedMbps;
        public long latencyMs;
        public int udpPacketsSent;
        public int udpPacketsReceived;
        public double udpPacketLossPercent;
        public double udpJitterMs;

        @Override
        public String toString() {
            return String.format("Download: %.2f Mbps | Upload: %.2f Mbps | Latency: %d ms | UDP Loss: %.1f%%",
                    downloadSpeedMbps, uploadSpeedMbps, latencyMs, udpPacketLossPercent);
        }
    }

    private final String host;
    private final int port;

    public BenchmarkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void runFullBenchmarkAsync(long testBytes, int udpCount, BenchmarkCallback callback) {
        new Thread(() -> {
            BenchmarkResult result = new BenchmarkResult();
            try {
                callback.onLog(String.format("Starting Benchmark against %s:%d...", host, port));

                // 1. Latency Check
                long t0 = System.currentTimeMillis();
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(host, port), 3000);
                    result.latencyMs = System.currentTimeMillis() - t0;
                }
                callback.onLog(String.format("TCP Connect Latency: %d ms", result.latencyMs));

                // 2. Download Test
                callback.onLog(String.format("Initiating TCP Download Test (%d MB payload)...", testBytes / (1024 * 1024)));
                result.downloadSpeedMbps = runDownloadTest(testBytes, callback);
                callback.onLog(String.format("Download Test Finished: %.2f Mbps (%.2f MB/s)",
                        result.downloadSpeedMbps, (result.downloadSpeedMbps / 8.0)));

                Thread.sleep(300);

                // 3. Upload Test
                callback.onLog(String.format("Initiating TCP Upload Test (%d MB payload)...", testBytes / (1024 * 1024)));
                result.uploadSpeedMbps = runUploadTest(testBytes, callback);
                callback.onLog(String.format("Upload Test Finished: %.2f Mbps (%.2f MB/s)",
                        result.uploadSpeedMbps, (result.uploadSpeedMbps / 8.0)));

                Thread.sleep(300);

                // 4. UDP Datagram Test
                callback.onLog(String.format("Initiating UDP Packet Communication Test (%d packets)...", udpCount));
                runUdpPacketTest(udpCount, result, callback);
                callback.onLog(String.format("UDP Test Finished: %d/%d received (%.1f%% loss, Jitter: %.2f ms)",
                        result.udpPacketsReceived, result.udpPacketsSent, result.udpPacketLossPercent, result.udpJitterMs));

                callback.onComplete(result);

            } catch (Exception e) {
                callback.onError("Benchmark failed: " + e.getMessage());
            }
        }, "BenchmarkClientThread").start();
    }

    private double runDownloadTest(long testBytes, BenchmarkCallback callback) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Send command 1 = DOWNLOAD
            dos.writeInt(1);
            dos.writeLong(testBytes);
            dos.flush();

            byte[] buffer = new byte[64 * 1024];
            long totalRead = 0;
            long startTime = System.nanoTime();
            long lastUpdate = System.currentTimeMillis();

            while (totalRead < testBytes) {
                int r = dis.read(buffer, 0, (int) Math.min(buffer.length, testBytes - totalRead));
                if (r == -1) break;
                totalRead += r;

                long now = System.currentTimeMillis();
                if (now - lastUpdate > 100) {
                    double elapsedSec = (System.nanoTime() - startTime) / 1_000_000_000.0;
                    double mbps = (elapsedSec > 0) ? (totalRead * 8.0) / (elapsedSec * 1_000_000.0) : 0;
                    int pct = (int) ((totalRead * 100.0) / testBytes);
                    callback.onProgress("Download", mbps, pct);
                    lastUpdate = now;
                }
            }

            double totalDurationSec = (System.nanoTime() - startTime) / 1_000_000_000.0;
            return (totalDurationSec > 0) ? (totalRead * 8.0) / (totalDurationSec * 1_000_000.0) : 0;
        }
    }

    private double runUploadTest(long testBytes, BenchmarkCallback callback) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Send command 2 = UPLOAD
            dos.writeInt(2);
            dos.writeLong(testBytes);
            dos.flush();

            byte[] buffer = new byte[64 * 1024];
            long totalSent = 0;
            long startTime = System.nanoTime();
            long lastUpdate = System.currentTimeMillis();

            while (totalSent < testBytes) {
                int toWrite = (int) Math.min(buffer.length, testBytes - totalSent);
                dos.write(buffer, 0, toWrite);
                totalSent += toWrite;

                long now = System.currentTimeMillis();
                if (now - lastUpdate > 100) {
                    double elapsedSec = (System.nanoTime() - startTime) / 1_000_000_000.0;
                    double mbps = (elapsedSec > 0) ? (totalSent * 8.0) / (elapsedSec * 1_000_000.0) : 0;
                    int pct = (int) ((totalSent * 100.0) / testBytes);
                    callback.onProgress("Upload", mbps, pct);
                    lastUpdate = now;
                }
            }
            dos.flush();

            // Wait for server acknowledgment
            long ackBytes = dis.readLong();
            double totalDurationSec = (System.nanoTime() - startTime) / 1_000_000_000.0;
            return (totalDurationSec > 0) ? (ackBytes * 8.0) / (totalDurationSec * 1_000_000.0) : 0;
        }
    }

    private void runUdpPacketTest(int packetCount, BenchmarkResult result, BenchmarkCallback callback) {
        result.udpPacketsSent = packetCount;
        int received = 0;
        double jitterSum = 0;
        long lastRtt = -1;
        int jitterCount = 0;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(300); // 300ms timeout per packet
            InetAddress targetAddr = InetAddress.getByName(host);

            byte[] recvBuf = new byte[1024];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);

            for (int seq = 1; seq <= packetCount; seq++) {
                ByteBuffer sendBuf = ByteBuffer.allocate(16);
                sendBuf.putInt(seq);
                long sendTime = System.currentTimeMillis();
                sendBuf.putLong(sendTime);

                byte[] data = sendBuf.array();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, targetAddr, port);
                socket.send(sendPacket);

                try {
                    socket.receive(recvPacket);
                    long rtt = System.currentTimeMillis() - sendTime;
                    received++;

                    if (lastRtt >= 0) {
                        jitterSum += Math.abs(rtt - lastRtt);
                        jitterCount++;
                    }
                    lastRtt = rtt;
                } catch (SocketTimeoutException ignored) {
                    // Packet dropped or timed out
                }

                if (seq % 5 == 0) {
                    int pct = (int) ((seq * 100.0) / packetCount);
                    callback.onProgress("UDP Packets", 0, pct);
                }

                Thread.sleep(15); // Small pacing between UDP datagrams
            }
        } catch (Exception e) {
            callback.onLog("UDP error: " + e.getMessage());
        }

        result.udpPacketsReceived = received;
        int lost = packetCount - received;
        result.udpPacketLossPercent = (packetCount == 0) ? 0 : ((double) lost / packetCount) * 100.0;
        result.udpJitterMs = (jitterCount == 0) ? 0 : (jitterSum / jitterCount);
    }
}
