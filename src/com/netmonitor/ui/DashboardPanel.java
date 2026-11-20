package com.netmonitor.ui;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.NetworkInterfaceInfo;
import com.netmonitor.model.SessionSummary;
import com.netmonitor.service.AlertService;
import com.netmonitor.service.NetworkInterfaceService;
import com.netmonitor.service.TrafficMonitorService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Primary Real-Time Dashboard view with KPI cards, live JFreeChart visualizer,
 * interface selector, and alert status notification bar.
 */
public class DashboardPanel extends JPanel {

    private final TrafficMonitorService monitorService;
    private final NetworkInterfaceService ifService;
    private final AlertService alertService;

    // KPI Cards
    private MetricCard downloadCard;
    private MetricCard uploadCard;
    private MetricCard sessionInCard;
    private MetricCard sessionOutCard;
    private MetricCard peakDownCard;
    private MetricCard interfaceCard;

    private BandwidthChartPanel chartPanel;
    private JComboBox<String> interfaceCombo;
    private JButton startStopBtn;
    private JLabel alertBanner;

    public DashboardPanel(TrafficMonitorService monitorService, NetworkInterfaceService ifService, AlertService alertService) {
        this.monitorService = monitorService;
        this.ifService = ifService;
        this.alertService = alertService;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARK);
        setBorder(UITheme.createPaddedBorder(16, 16, 16, 16));

        initTopControls();
        initKpiGrid();
        initChartAndAlerts();

        // Register listeners
        monitorService.addListener(this::onSampleReceived);
        alertService.addListener(this::onAlertReceived);
    }

    private void initTopControls() {
        JPanel controlPanel = new JPanel(new BorderLayout(12, 0));
        controlPanel.setOpaque(false);

        // Title on the left
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Real-Time Bandwidth Monitor");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        titleBox.add(title);

        // Controls on the right
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBox.setOpaque(false);

        JLabel ifLabel = new JLabel("Interface:");
        ifLabel.setFont(UITheme.FONT_BODY);
        ifLabel.setForeground(UITheme.TEXT_MUTED);
        actionBox.add(ifLabel);

        interfaceCombo = new JComboBox<>();
        interfaceCombo.setFont(UITheme.FONT_BODY);
        interfaceCombo.setBackground(UITheme.BG_CARD);
        interfaceCombo.setForeground(UITheme.TEXT_PRIMARY);
        populateInterfaces();
        interfaceCombo.addActionListener(e -> {
            String selected = (String) interfaceCombo.getSelectedItem();
            if (selected != null) {
                if (selected.startsWith("All")) {
                    monitorService.setTargetInterface("all");
                } else {
                    String ifName = selected.split(" ")[0].trim();
                    monitorService.setTargetInterface(ifName);
                }
            }
        });
        actionBox.add(interfaceCombo);

        // Interval
        JLabel intLabel = new JLabel("Interval:");
        intLabel.setFont(UITheme.FONT_BODY);
        intLabel.setForeground(UITheme.TEXT_MUTED);
        actionBox.add(intLabel);

        JComboBox<String> intervalCombo = new JComboBox<>(new String[]{"500 ms", "1.0 s", "2.0 s"});
        intervalCombo.setSelectedIndex(1);
        intervalCombo.setFont(UITheme.FONT_BODY);
        intervalCombo.setBackground(UITheme.BG_CARD);
        intervalCombo.setForeground(UITheme.TEXT_PRIMARY);
        intervalCombo.addActionListener(e -> {
            int idx = intervalCombo.getSelectedIndex();
            int ms = (idx == 0) ? 500 : (idx == 1 ? 1000 : 2000);
            monitorService.setPollingIntervalMs(ms);
        });
        actionBox.add(intervalCombo);

        // Start / Pause
        startStopBtn = UITheme.createStyledButton("Pause", UITheme.ACCENT_AMBER);
        startStopBtn.addActionListener(e -> {
            if (monitorService.isRunning()) {
                monitorService.stop();
                startStopBtn.setText("Resume");
                startStopBtn.setBackground(UITheme.ACCENT_EMERALD);
            } else {
                monitorService.start();
                startStopBtn.setText("Pause");
                startStopBtn.setBackground(UITheme.ACCENT_AMBER);
            }
        });
        actionBox.add(startStopBtn);

        controlPanel.add(titleBox, BorderLayout.WEST);
        controlPanel.add(actionBox, BorderLayout.EAST);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void populateInterfaces() {
        interfaceCombo.removeAllItems();
        interfaceCombo.addItem("All Interfaces (Aggregate)");
        List<NetworkInterfaceInfo> ifaces = ifService.getAllInterfaces();
        String primaryName = null;
        for (NetworkInterfaceInfo iface : ifaces) {
            if (iface.isUp() && !iface.isLoopback()) {
                String label = iface.getName() + " (" + iface.getPrimaryIpv4() + ")";
                interfaceCombo.addItem(label);
                if (primaryName == null && !iface.getIpv4Addresses().isEmpty()) {
                    primaryName = label;
                }
            }
        }
        if (primaryName != null) {
            interfaceCombo.setSelectedItem(primaryName);
            String ifName = primaryName.split(" ")[0].trim();
            monitorService.setTargetInterface(ifName);
        }
    }

    private void initKpiGrid() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 6, 10, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(kpiPanel.getWidth(), 85));

        downloadCard = new MetricCard("Download Speed", "0.00 KB/s", "0.00 Mbps", UITheme.ACCENT_CYAN);
        uploadCard = new MetricCard("Upload Speed", "0.00 KB/s", "0.00 Mbps", UITheme.ACCENT_EMERALD);
        sessionInCard = new MetricCard("Session Received", "0.00 MB", "Packets: 0", UITheme.ACCENT_CYAN);
        sessionOutCard = new MetricCard("Session Sent", "0.00 MB", "Packets: 0", UITheme.ACCENT_EMERALD);
        peakDownCard = new MetricCard("Peak Download", "0.00 KB/s", "Session Peak", UITheme.ACCENT_PURPLE);
        interfaceCard = new MetricCard("Active Interface", "en0", "Status: UP", UITheme.TEXT_SECONDARY);

        kpiPanel.add(downloadCard);
        kpiPanel.add(uploadCard);
        kpiPanel.add(sessionInCard);
        kpiPanel.add(sessionOutCard);
        kpiPanel.add(peakDownCard);
        kpiPanel.add(interfaceCard);

        // Put between North and Center
        JPanel centerWrap = new JPanel(new BorderLayout(0, 10));
        centerWrap.setOpaque(false);
        centerWrap.add(kpiPanel, BorderLayout.NORTH);

        chartPanel = new BandwidthChartPanel();
        centerWrap.add(chartPanel, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);
    }

    private void initChartAndAlerts() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(UITheme.BG_CARD);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        alertBanner = new JLabel("● System Normal — Bandwidth monitoring active, all metrics within defined safety thresholds.");
        alertBanner.setFont(UITheme.FONT_BODY);
        alertBanner.setForeground(UITheme.ACCENT_EMERALD);

        bottomBar.add(alertBanner, BorderLayout.WEST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void onSampleReceived(BandwidthSample sample) {
        // Update KPI cards
        downloadCard.updateValues(sample.getFormattedDownloadSpeed(), String.format("%.2f Mbps", sample.getDownloadMbps()));
        uploadCard.updateValues(sample.getFormattedUploadSpeed(), String.format("%.2f Mbps", sample.getUploadMbps()));

        SessionSummary session = monitorService.getCurrentSession();
        if (session != null) {
            sessionInCard.updateValues(
                    BandwidthSample.formatBytes(session.getTotalBytesIn()),
                    "Packets: " + sample.getPacketsIn()
            );
            sessionOutCard.updateValues(
                    BandwidthSample.formatBytes(session.getTotalBytesOut()),
                    "Packets: " + sample.getPacketsOut()
            );
            peakDownCard.updateValues(
                    BandwidthSample.formatSpeed(session.getPeakDownSpeedBps()),
                    "Avg: " + BandwidthSample.formatSpeed(session.getAvgDownSpeedBps())
            );
        }

        interfaceCard.updateValues(sample.getInterfaceName(), "Monitoring");
        chartPanel.addSample(sample);
    }

    private void onAlertReceived(AlertRecord alert) {
        String prefix = alert.getLevel() == AlertRecord.Level.CRITICAL ? "▲ CRITICAL ALERT: " :
                        (alert.getLevel() == AlertRecord.Level.WARNING ? "⚠ WARNING: " : "ℹ NOTICE: ");
        Color color = alert.getLevel() == AlertRecord.Level.CRITICAL ? UITheme.ACCENT_ROSE :
                      (alert.getLevel() == AlertRecord.Level.WARNING ? UITheme.ACCENT_AMBER : UITheme.ACCENT_CYAN);

        alertBanner.setText(prefix + alert.getMessage());
        alertBanner.setForeground(color);
    }
}
