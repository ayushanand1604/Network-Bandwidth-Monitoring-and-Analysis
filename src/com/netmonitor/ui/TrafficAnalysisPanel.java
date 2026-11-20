package com.netmonitor.ui;

import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.SessionSummary;
import com.netmonitor.service.TrafficMonitorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Detailed network traffic analysis view.
 * Analyzes incoming/outgoing traffic distribution, packet rates, cumulative statistics,
 * and throughput ratios.
 */
public class TrafficAnalysisPanel extends JPanel {

    private final TrafficMonitorService monitorService;
    private final DecimalFormat DF = new DecimalFormat("#,##0.00");

    private JProgressBar trafficRatioBar;
    private JLabel ratioLabel;
    private JLabel totalTransferredLabel;
    private JLabel rxPktsLabel;
    private JLabel txPktsLabel;

    private DefaultTableModel statsTableModel;

    public TrafficAnalysisPanel(TrafficMonitorService monitorService) {
        this.monitorService = monitorService;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(UITheme.createPaddedBorder(16, 16, 16, 16));

        initHeader();
        initContent();

        monitorService.addListener(this::onSampleReceived);
    }

    private void initHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Network Traffic & Protocol Analysis");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel mainGrid = new JPanel(new GridLayout(2, 1, 16, 16));
        mainGrid.setOpaque(false);

        // Top Section: Traffic Distribution & Packet Metrics
        JPanel topCard = new JPanel(new BorderLayout(12, 12));
        topCard.setBackground(UITheme.BG_CARD);
        topCard.setBorder(UITheme.createCardBorder());

        JLabel topTitle = new JLabel("DATA FLOW & TRAFFIC RATIO");
        topTitle.setFont(UITheme.FONT_SUBHEADER);
        topTitle.setForeground(UITheme.TEXT_MUTED);
        topCard.add(topTitle, BorderLayout.NORTH);

        JPanel ratioPanel = new JPanel();
        ratioPanel.setLayout(new BoxLayout(ratioPanel, BoxLayout.Y_AXIS));
        ratioPanel.setOpaque(false);

        trafficRatioBar = new JProgressBar(0, 100);
        trafficRatioBar.setValue(50);
        trafficRatioBar.setStringPainted(true);
        trafficRatioBar.setFont(UITheme.FONT_HEADER);
        trafficRatioBar.setForeground(UITheme.ACCENT_CYAN);
        trafficRatioBar.setBackground(UITheme.ACCENT_EMERALD);
        trafficRatioBar.setPreferredSize(new Dimension(trafficRatioBar.getWidth(), 30));

        ratioLabel = new JLabel("Incoming: 50.0%  |  Outgoing: 50.0%");
        ratioLabel.setFont(UITheme.FONT_BODY);
        ratioLabel.setForeground(UITheme.TEXT_SECONDARY);

        totalTransferredLabel = new JLabel("Total Session Data: 0.00 MB");
        totalTransferredLabel.setFont(UITheme.FONT_HEADER);
        totalTransferredLabel.setForeground(UITheme.TEXT_PRIMARY);

        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        metricsRow.setOpaque(false);

        rxPktsLabel = new JLabel("Inbound Packets: 0");
        rxPktsLabel.setFont(UITheme.FONT_BODY);
        rxPktsLabel.setForeground(UITheme.ACCENT_CYAN);

        txPktsLabel = new JLabel("Outbound Packets: 0");
        txPktsLabel.setFont(UITheme.FONT_BODY);
        txPktsLabel.setForeground(UITheme.ACCENT_EMERALD);

        metricsRow.add(totalTransferredLabel);
        metricsRow.add(rxPktsLabel);
        metricsRow.add(txPktsLabel);

        ratioPanel.add(Box.createVerticalStrut(10));
        ratioPanel.add(trafficRatioBar);
        ratioPanel.add(Box.createVerticalStrut(8));
        ratioPanel.add(ratioLabel);
        ratioPanel.add(Box.createVerticalStrut(16));
        ratioPanel.add(metricsRow);

        topCard.add(ratioPanel, BorderLayout.CENTER);
        mainGrid.add(topCard);

        // Bottom Section: Throughput & Speed Breakdown Table
        JPanel tableCard = new JPanel(new BorderLayout(12, 12));
        tableCard.setBackground(UITheme.BG_CARD);
        tableCard.setBorder(UITheme.createCardBorder());

        JLabel tableTitle = new JLabel("THROUGHPUT & TRANSMISSION METRICS TABLE");
        tableTitle.setFont(UITheme.FONT_SUBHEADER);
        tableTitle.setForeground(UITheme.TEXT_MUTED);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Metric Direction", "Current Speed", "Current (Mbps)", "Peak Speed", "Average Speed", "Total Volume"};
        statsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        statsTableModel.addRow(new Object[]{"Download (Inbound)", "0.00 KB/s", "0.00 Mbps", "0.00 KB/s", "0.00 KB/s", "0.00 MB"});
        statsTableModel.addRow(new Object[]{"Upload (Outbound)", "0.00 KB/s", "0.00 Mbps", "0.00 KB/s", "0.00 KB/s", "0.00 MB"});
        statsTableModel.addRow(new Object[]{"Combined Aggregate", "0.00 KB/s", "0.00 Mbps", "0.00 KB/s", "0.00 KB/s", "0.00 MB"});

        JTable table = new JTable(statsTableModel);
        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        tableCard.add(scrollPane, BorderLayout.CENTER);
        mainGrid.add(tableCard);

        add(mainGrid, BorderLayout.CENTER);
    }

    private void onSampleReceived(BandwidthSample sample) {
        SessionSummary session = monitorService.getCurrentSession();
        if (session == null) return;

        long inBytes = session.getTotalBytesIn();
        long outBytes = session.getTotalBytesOut();
        long totalBytes = inBytes + outBytes;

        if (totalBytes > 0) {
            double rxPct = ((double) inBytes / totalBytes) * 100.0;
            double txPct = ((double) outBytes / totalBytes) * 100.0;
            trafficRatioBar.setValue((int) rxPct);
            trafficRatioBar.setString(String.format("Download: %.1f%%  |  Upload: %.1f%%", rxPct, txPct));
            ratioLabel.setText(String.format("Inbound Data: %s (%.1f%%)   &bull;   Outbound Data: %s (%.1f%%)",
                    BandwidthSample.formatBytes(inBytes), rxPct, BandwidthSample.formatBytes(outBytes), txPct));
        }

        totalTransferredLabel.setText("Total Transferred: " + BandwidthSample.formatBytes(totalBytes));
        rxPktsLabel.setText("Inbound Packets: " + sample.getPacketsIn());
        txPktsLabel.setText("Outbound Packets: " + sample.getPacketsOut());

        // Update Table
        // Row 0: Download
        statsTableModel.setValueAt(sample.getFormattedDownloadSpeed(), 0, 1);
        statsTableModel.setValueAt(String.format("%.2f Mbps", sample.getDownloadMbps()), 0, 2);
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(session.getPeakDownSpeedBps()), 0, 3);
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(session.getAvgDownSpeedBps()), 0, 4);
        statsTableModel.setValueAt(BandwidthSample.formatBytes(session.getTotalBytesIn()), 0, 5);

        // Row 1: Upload
        statsTableModel.setValueAt(sample.getFormattedUploadSpeed(), 1, 1);
        statsTableModel.setValueAt(String.format("%.2f Mbps", sample.getUploadMbps()), 1, 2);
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(session.getPeakUpSpeedBps()), 1, 3);
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(session.getAvgUpSpeedBps()), 1, 4);
        statsTableModel.setValueAt(BandwidthSample.formatBytes(session.getTotalBytesOut()), 1, 5);

        // Row 2: Combined
        double combinedSpeed = sample.getDownloadSpeedBps() + sample.getUploadSpeedBps();
        double combinedPeak = session.getPeakDownSpeedBps() + session.getPeakUpSpeedBps();
        double combinedAvg = session.getAvgDownSpeedBps() + session.getAvgUpSpeedBps();
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(combinedSpeed), 2, 1);
        statsTableModel.setValueAt(String.format("%.2f Mbps", (combinedSpeed * 8.0) / 1_000_000.0), 2, 2);
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(combinedPeak), 2, 3);
        statsTableModel.setValueAt(BandwidthSample.formatSpeed(combinedAvg), 2, 4);
        statsTableModel.setValueAt(BandwidthSample.formatBytes(totalBytes), 2, 5);
    }
}
