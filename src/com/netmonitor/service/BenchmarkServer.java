package com.netmonitor.service;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Embedded multi-threaded TCP/UDP Benchmark Server for demonstrating Computer Networks concepts:
 * - Client-Server Architecture
 * - TCP Stream Transmission & Throughput
 * - UDP Datagram Packet Communication
 */
public class BenchmarkServer {

    public interface ServerLogListener {
        void onServerLog(String message);
    }

    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;
    private ExecutorService threadPool;
    private ServerLogListener logListener;

    public BenchmarkServer(int port) {
        this.port = port;
    }

    public void setLogListener(ServerLogListener listener) {
        this.logListener = listener;
    }

    public boolean isRunning() {
        return running.get();
    }

    public synchronized void start() throws IOException {
        if (running.get()) return;
        running.set(true);

        tcpServerSocket = new ServerSocket(port);
        udpSocket = new DatagramSocket(port);
        threadPool = Executors.newCachedThreadPool();

        log("TCP Server listening on port " + port);
        log("UDP Server listening on port " + port);

        // TCP Worker
        threadPool.execute(() -> {
            while (running.get()) {
                try {
                    Socket clientSocket = tcpServerSocket.accept();
                    log("Accepted TCP connection from " + clientSocket.getRemoteSocketAddress());
                    threadPool.execute(() -> handleTcpClient(clientSocket));
                } catch (IOException e) {
                    if (running.get()) {
                        log("TCP accept error: " + e.getMessage());
                    }
                }
            }
        });

        // UDP Worker
        threadPool.execute(() -> {
            byte[] buf = new byte[4096];
            while (running.get()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    udpSocket.receive(packet);
                    // Echo back with timestamp
                    DatagramPacket response = new DatagramPacket(
                            packet.getData(), packet.getLength(),
                            packet.getAddress(), packet.getPort()
                    );
                    udpSocket.send(response);
                } catch (IOException e) {
                    if (running.get()) {
                        log("UDP error: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void handleTcpClient(Socket socket) {
        try (socket;
             DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            // Protocol command byte: 1 = DOWNLOAD_TEST (Server sends to Client), 2 = UPLOAD_TEST (Client sends to Server)
            int cmd = dis.readInt();
            if (cmd == 1) { // Client requests DOWNLOAD test
                long bytesToSend = dis.readLong();
                log(String.format("Client %s requested %d bytes download", socket.getRemoteSocketAddress(), bytesToSend));
                byte[] chunk = new byte[64 * 1024]; // 64 KB buffer
                long remaining = bytesToSend;
                while (remaining > 0 && running.get()) {
                    int toWrite = (int) Math.min(chunk.length, remaining);
                    dos.write(chunk, 0, toWrite);
                    remaining -= toWrite;
                }
                dos.flush();
                log("Finished sending data to " + socket.getRemoteSocketAddress());
            } else if (cmd == 2) { // Client requests UPLOAD test
                long expectedBytes = dis.readLong();
                log(String.format("Client %s uploading %d bytes", socket.getRemoteSocketAddress(), expectedBytes));
                byte[] chunk = new byte[64 * 1024];
                long totalRead = 0;
                while (totalRead < expectedBytes) {
                    int r = dis.read(chunk, 0, (int) Math.min(chunk.length, expectedBytes - totalRead));
                    if (r == -1) break;
                    totalRead += r;
                }
                dos.writeLong(totalRead); // ACK bytes received
                dos.flush();
                log(String.format("Finished receiving %d bytes from %s", totalRead, socket.getRemoteSocketAddress()));
            }
        } catch (IOException e) {
            log("Client error: " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (!running.get()) return;
        running.set(false);
        try {
            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
            }
        } catch (IOException ignored) {}
        try {
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
        } catch (Exception ignored) {}
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
        log("Server stopped successfully.");
    }

    private void log(String msg) {
        if (logListener != null) {
            logListener.onServerLog("[Server] " + msg);
        }
    }
}
