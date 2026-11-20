package com.netmonitor.ui;

import com.netmonitor.model.PingResult;
import com.netmonitor.service.BenchmarkClient;
import com.netmonitor.service.BenchmarkServer;
import com.netmonitor.service.PingService;

import javax.swing.*;
import java.awt.*;

/**
 * Computer Networks Experimental Lab & Practical Demonstrator.
 * Explicitly implements and showcases:
 * 1. Latency & Jitter (Ping) Measurement
 * 2. Client-Server Architecture (TCP Multi-threaded Server + Client)
 * 3. Actual Throughput Calculation (Download & Upload Mbps)
 * 4. Packet Communication (UDP Datagram Transmission & Packet Loss Analysis)
 */
public class CNLabPanel extends JPanel {

    private final PingService pingService;
    private final BenchmarkServer benchmarkServer;

    // Ping UI elements
    private JTextField pingHostField;
    private JComboBox<Integer> pingCountCombo;
    private JButton startPingBtn;
    private JTextArea pingLogArea;
    private JLabel pingSummaryLabel;

    // Benchmark UI elements
    private JButton toggleServerBtn;
    private JLabel serverStatusLabel;
    private JTextField clientHostField;
    private JTextField clientPortField;
    private JComboBox<String> payloadCombo;
    private JButton runBenchmarkBtn;
    private JProgressBar benchmarkProgress;
    private JTextArea benchmarkLogArea;
    private JLabel benchmarkResultLabel;

    public CNLabPanel() {
        this.pingService = new PingService();
        this.benchmarkServer = new BenchmarkServer(9876);

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(UITheme.createPaddedBorder(16, 16, 16, 16));

        initHeader();
        initContent();
    }

    private void initHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Computer Networks Experimental Lab & Protocol Benchmarks");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        mainPanel.setOpaque(false);

        // Left Panel: Ping, Latency & Jitter Tool
        mainPanel.add(createPingPanel());

        // Right Panel: Client-Server & Throughput Benchmark
        mainPanel.add(createBenchmarkPanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createPingPanel() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(UITheme.createCardBorder());

        // Header
        JLabel title = new JLabel("LATENCY, RTT & JITTER PROBE (PING)");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.TEXT_MUTED);
        card.add(title, BorderLayout.NORTH);

        // Form controls
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setOpaque(false);

        JLabel hostLbl = new JLabel("Host/IP:");
        hostLbl.setFont(UITheme.FONT_BODY);
        hostLbl.setForeground(UITheme.TEXT_PRIMARY);
        form.add(hostLbl);

        pingHostField = new JTextField("8.8.8.8", 10);
        pingHostField.setFont(UITheme.FONT_BODY);
        pingHostField.setBackground(new Color(20, 27, 45));
        pingHostField.setForeground(UITheme.TEXT_PRIMARY);
        form.add(pingHostField);

        JLabel countLbl = new JLabel("Count:");
        countLbl.setFont(UITheme.FONT_BODY);
        countLbl.setForeground(UITheme.TEXT_PRIMARY);
        form.add(countLbl);

        pingCountCombo = new JComboBox<>(new Integer[]{4, 8, 16, 30});
        pingCountCombo.setFont(UITheme.FONT_BODY);
        pingCountCombo.setBackground(UITheme.BG_CARD);
        pingCountCombo.setForeground(UITheme.TEXT_PRIMARY);
        form.add(pingCountCombo);

        startPingBtn = UITheme.createStyledButton("Start Ping", UITheme.ACCENT_CYAN);
        startPingBtn.addActionListener(e -> runPing());
        form.add(startPingBtn);

        // Center Log
        pingLogArea = new JTextArea();
        pingLogArea.setFont(UITheme.FONT_MONO);
        pingLogArea.setBackground(new Color(20, 27, 45));
        pingLogArea.setForeground(UITheme.ACCENT_CYAN);
        pingLogArea.setEditable(false);
        pingLogArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(pingLogArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        // Summary Label
        pingSummaryLabel = new JLabel("Ready. Enter target host to measure round trip time (RTT).");
        pingSummaryLabel.setFont(UITheme.FONT_SMALL);
        pingSummaryLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel centerWrap = new JPanel(new BorderLayout(6, 6));
        centerWrap.setOpaque(false);
        centerWrap.add(form, BorderLayout.NORTH);
        centerWrap.add(scroll, BorderLayout.CENTER);
        centerWrap.add(pingSummaryLabel, BorderLayout.SOUTH);

        card.add(centerWrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBenchmarkPanel() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(UITheme.createCardBorder());

        // Header
        JLabel title = new JLabel("CLIENT-SERVER THROUGHPUT & PACKET BENCHMARK");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.TEXT_MUTED);
        card.add(title, BorderLayout.NORTH);

        // Top server control bar
        JPanel serverBar = new JPanel(new BorderLayout(8, 0));
        serverBar.setBackground(UITheme.BG_CARD_LIGHT);
        serverBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        serverStatusLabel = new JLabel("● Server Stopped (Port 9876)");
        serverStatusLabel.setFont(UITheme.FONT_BODY);
        serverStatusLabel.setForeground(UITheme.TEXT_MUTED);

        toggleServerBtn = UITheme.createStyledButton("Start Server", UITheme.ACCENT_EMERALD);
        toggleServerBtn.addActionListener(e -> toggleServer());

        serverBar.add(serverStatusLabel, BorderLayout.WEST);
        serverBar.add(toggleServerBtn, BorderLayout.EAST);

        // Client controls
        JPanel clientControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        clientControls.setOpaque(false);

        JLabel hLbl = new JLabel("Server:");
        hLbl.setFont(UITheme.FONT_BODY);
        hLbl.setForeground(UITheme.TEXT_PRIMARY);
        clientControls.add(hLbl);

        clientHostField = new JTextField("127.0.0.1", 8);
        clientHostField.setFont(UITheme.FONT_BODY);
        clientHostField.setBackground(new Color(20, 27, 45));
        clientHostField.setForeground(UITheme.TEXT_PRIMARY);
        clientControls.add(clientHostField);

        JLabel pLbl = new JLabel("Port:");
        pLbl.setFont(UITheme.FONT_BODY);
        pLbl.setForeground(UITheme.TEXT_PRIMARY);
        clientControls.add(pLbl);

        clientPortField = new JTextField("9876", 4);
        clientPortField.setFont(UITheme.FONT_BODY);
        clientPortField.setBackground(new Color(20, 27, 45));
        clientPortField.setForeground(UITheme.TEXT_PRIMARY);
        clientControls.add(clientPortField);

        JLabel sizeLbl = new JLabel("Size:");
        sizeLbl.setFont(UITheme.FONT_BODY);
        sizeLbl.setForeground(UITheme.TEXT_PRIMARY);
        clientControls.add(sizeLbl);

        payloadCombo = new JComboBox<>(new String[]{"2 MB", "5 MB", "10 MB", "20 MB"});
        payloadCombo.setSelectedIndex(1);
        payloadCombo.setFont(UITheme.FONT_BODY);
        payloadCombo.setBackground(UITheme.BG_CARD);
        payloadCombo.setForeground(UITheme.TEXT_PRIMARY);
        clientControls.add(payloadCombo);

        runBenchmarkBtn = UITheme.createStyledButton("Run Benchmark", UITheme.ACCENT_PURPLE);
        runBenchmarkBtn.addActionListener(e -> runBenchmark());
        clientControls.add(runBenchmarkBtn);

        // Progress bar
        benchmarkProgress = new JProgressBar(0, 100);
        benchmarkProgress.setValue(0);
        benchmarkProgress.setStringPainted(true);
        benchmarkProgress.setFont(UITheme.FONT_SMALL);
        benchmarkProgress.setForeground(UITheme.ACCENT_PURPLE);

        // Benchmark Log
        benchmarkLogArea = new JTextArea();
        benchmarkLogArea.setFont(UITheme.FONT_MONO);
        benchmarkLogArea.setBackground(new Color(20, 27, 45));
        benchmarkLogArea.setForeground(UITheme.TEXT_PRIMARY);
        benchmarkLogArea.setEditable(false);
        benchmarkLogArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane benchScroll = new JScrollPane(benchmarkLogArea);
        benchScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        benchmarkResultLabel = new JLabel("Throughput: Idle (Start local server then test throughput)");
        benchmarkResultLabel.setFont(UITheme.FONT_HEADER);
        benchmarkResultLabel.setForeground(UITheme.ACCENT_EMERALD);

        JPanel centerWrap = new JPanel(new BorderLayout(6, 6));
        centerWrap.setOpaque(false);
        centerWrap.add(serverBar, BorderLayout.NORTH);

        JPanel subCenter = new JPanel(new BorderLayout(6, 6));
        subCenter.setOpaque(false);
        subCenter.add(clientControls, BorderLayout.NORTH);
        subCenter.add(benchScroll, BorderLayout.CENTER);

        JPanel bottomBox = new JPanel();
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));
        bottomBox.setOpaque(false);
        bottomBox.add(benchmarkProgress);
        bottomBox.add(Box.createVerticalStrut(6));
        bottomBox.add(benchmarkResultLabel);

        subCenter.add(bottomBox, BorderLayout.SOUTH);
        centerWrap.add(subCenter, BorderLayout.CENTER);

        card.add(centerWrap, BorderLayout.CENTER);
        return card;
    }

    private void runPing() {
        String host = pingHostField.getText().trim();
        if (host.isEmpty()) return;

        int count = (Integer) pingCountCombo.getSelectedItem();
        startPingBtn.setEnabled(false);
        pingLogArea.setText(String.format("Pinging %s with 32 bytes of application payload:\n\n", host));
        pingSummaryLabel.setText("Pinging " + host + "...");

        pingService.executePingAsync(host, count, 2000, new PingService.PingCallback() {
            @Override
            public void onPingResult(PingResult result) {
                SwingUtilities.invokeLater(() -> pingLogArea.append(result.toString() + "\n"));
            }

            @Override
            public void onPingComplete(PingService.PingStatistics stats) {
                SwingUtilities.invokeLater(() -> {
                    pingLogArea.append("\n--- Ping Statistics for " + stats.host + " ---\n");
                    pingLogArea.append(String.format("Packets: Sent = %d, Received = %d, Lost = %d (%.1f%% loss)\n",
                            stats.transmitted, stats.received, stats.lost, stats.lossRatePercent));
                    pingLogArea.append(String.format("Approximate Round Trip Times: Min = %dms, Max = %dms, Avg = %.1fms\n",
                            stats.minRtt, stats.maxRtt, stats.avgRtt));
                    pingLogArea.append(String.format("Jitter (Mean Absolute Deviation): %.2f ms\n", stats.jitter));

                    pingSummaryLabel.setText(String.format("Avg RTT: %.1f ms | Loss: %.1f%% | Jitter: %.2f ms",
                            stats.avgRtt, stats.lossRatePercent, stats.jitter));
                    startPingBtn.setEnabled(true);
                });
            }
        });
    }

    private void toggleServer() {
        if (benchmarkServer.isRunning()) {
            benchmarkServer.stop();
            toggleServerBtn.setText("Start Server");
            toggleServerBtn.setBackground(UITheme.ACCENT_EMERALD);
            serverStatusLabel.setText("● Server Stopped (Port 9876)");
            serverStatusLabel.setForeground(UITheme.TEXT_MUTED);
        } else {
            try {
                benchmarkServer.setLogListener(msg -> SwingUtilities.invokeLater(() -> benchmarkLogArea.append(msg + "\n")));
                benchmarkServer.start();
                toggleServerBtn.setText("Stop Server");
                toggleServerBtn.setBackground(UITheme.ACCENT_ROSE);
                serverStatusLabel.setText("● Server Running on TCP/UDP Port 9876 (Ready)");
                serverStatusLabel.setForeground(UITheme.ACCENT_EMERALD);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to start server: " + e.getMessage(), "Server Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void runBenchmark() {
        // Auto-start server if testing localhost and not running
        String host = clientHostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(clientPortField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ((host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost")) && !benchmarkServer.isRunning()) {
            toggleServer();
        }

        int payloadIdx = payloadCombo.getSelectedIndex();
        int mb = (payloadIdx == 0) ? 2 : (payloadIdx == 1 ? 5 : (payloadIdx == 2 ? 10 : 20));
        long testBytes = mb * 1024L * 1024L;

        runBenchmarkBtn.setEnabled(false);
        benchmarkLogArea.setText("");
        benchmarkProgress.setValue(0);
        benchmarkProgress.setString("Running Benchmark...");

        BenchmarkClient client = new BenchmarkClient(host, port);
        client.runFullBenchmarkAsync(testBytes, 40, new BenchmarkClient.BenchmarkCallback() {
            @Override
            public void onProgress(String phase, double currentMbps, int percentComplete) {
                SwingUtilities.invokeLater(() -> {
                    benchmarkProgress.setValue(percentComplete);
                    benchmarkProgress.setString(String.format("%s: %d%% (%.1f Mbps)", phase, percentComplete, currentMbps));
                });
            }

            @Override
            public void onLog(String message) {
                SwingUtilities.invokeLater(() -> benchmarkLogArea.append(message + "\n"));
            }

            @Override
            public void onComplete(BenchmarkClient.BenchmarkResult result) {
                SwingUtilities.invokeLater(() -> {
                    runBenchmarkBtn.setEnabled(true);
                    benchmarkProgress.setValue(100);
                    benchmarkProgress.setString("Benchmark Complete!");

                    String summary = String.format("DL: %.2f Mbps  |  UL: %.2f Mbps  |  RTT: %d ms  |  UDP Loss: %.1f%%",
                            result.downloadSpeedMbps, result.uploadSpeedMbps, result.latencyMs, result.udpPacketLossPercent);
                    benchmarkResultLabel.setText(summary);
                });
            }

            @Override
            public void onError(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    runBenchmarkBtn.setEnabled(true);
                    benchmarkProgress.setValue(0);
                    benchmarkProgress.setString("Error occurred");
                    benchmarkLogArea.append("\n[ERROR] " + errorMessage + "\n");
                    JOptionPane.showMessageDialog(CNLabPanel.this, errorMessage, "Benchmark Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    public void cleanup() {
        if (benchmarkServer.isRunning()) {
            benchmarkServer.stop();
        }
    }
}
