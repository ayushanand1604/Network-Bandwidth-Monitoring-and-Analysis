package com.netmonitor.ui;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.service.AlertService;
import com.netmonitor.service.DatabaseService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Bandwidth Alerts Configuration & Alert Log Viewer.
 * Allows setting bandwidth usage limits, detecting sudden network traffic spikes,
 * and reviewing warning history.
 */
public class AlertsPanel extends JPanel {

    private final AlertService alertService;
    private final DatabaseService dbService;

    private JSpinner speedSpinner;
    private JSpinner quotaSpinner;
    private JCheckBox spikeCheck;
    private JCheckBox audioCheck;

    private DefaultTableModel alertTableModel;
    private JTable alertTable;

    public AlertsPanel(AlertService alertService, DatabaseService dbService) {
        this.alertService = alertService;
        this.dbService = dbService;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(UITheme.createPaddedBorder(16, 16, 16, 16));

        initHeader();
        initContent();

        alertService.addListener(this::onAlertTriggered);
        loadAlertsFromDb();
    }

    private void initHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Bandwidth Thresholds & Anomaly Alerts");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel mainGrid = new JPanel(new BorderLayout(16, 16));
        mainGrid.setOpaque(false);

        // Top Settings Card
        JPanel settingsCard = new JPanel(new BorderLayout(12, 12));
        settingsCard.setBackground(UITheme.BG_CARD);
        settingsCard.setBorder(UITheme.createCardBorder());

        JLabel settingsTitle = new JLabel("ALERT THRESHOLD CONFIGURATION");
        settingsTitle.setFont(UITheme.FONT_SUBHEADER);
        settingsTitle.setForeground(UITheme.TEXT_MUTED);
        settingsCard.add(settingsTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        formGrid.setOpaque(false);

        // Speed Limit
        formGrid.add(new JLabel("Max Speed Limit (MB/s):") {{
            setFont(UITheme.FONT_BODY);
            setForeground(UITheme.TEXT_PRIMARY);
        }});
        speedSpinner = new JSpinner(new SpinnerNumberModel(alertService.getSpeedLimitMBps(), 1.0, 500.0, 1.0));
        speedSpinner.setFont(UITheme.FONT_BODY);
        formGrid.add(speedSpinner);

        // Quota Limit
        formGrid.add(new JLabel("Session Quota (MB):") {{
            setFont(UITheme.FONT_BODY);
            setForeground(UITheme.TEXT_PRIMARY);
        }});
        quotaSpinner = new JSpinner(new SpinnerNumberModel(alertService.getSessionQuotaMB(), 10.0, 50000.0, 50.0));
        quotaSpinner.setFont(UITheme.FONT_BODY);
        formGrid.add(quotaSpinner);

        // Spike Detection
        spikeCheck = new JCheckBox("Spike Anomaly Detection", alertService.isSpikeDetectionEnabled());
        spikeCheck.setFont(UITheme.FONT_BODY);
        spikeCheck.setForeground(UITheme.TEXT_PRIMARY);
        spikeCheck.setOpaque(false);
        formGrid.add(spikeCheck);

        // Audio Alerts
        audioCheck = new JCheckBox("Audio Beep Warning", alertService.isAudioAlertsEnabled());
        audioCheck.setFont(UITheme.FONT_BODY);
        audioCheck.setForeground(UITheme.TEXT_PRIMARY);
        audioCheck.setOpaque(false);
        formGrid.add(audioCheck);

        // Action Buttons
        JButton saveBtn = UITheme.createStyledButton("Save Thresholds", UITheme.ACCENT_CYAN);
        saveBtn.addActionListener(e -> saveSettings());
        formGrid.add(saveBtn);

        JButton testBtn = UITheme.createStyledButton("Trigger Test Alert", UITheme.ACCENT_AMBER);
        testBtn.addActionListener(e -> alertService.triggerManualTestAlert());
        formGrid.add(testBtn);

        JButton clearBtn = UITheme.createStyledButton("Clear Log", UITheme.ACCENT_ROSE);
        clearBtn.addActionListener(e -> clearAlerts());
        formGrid.add(clearBtn);

        settingsCard.add(formGrid, BorderLayout.CENTER);
        mainGrid.add(settingsCard, BorderLayout.NORTH);

        // Center: Alert Table
        JPanel tableCard = new JPanel(new BorderLayout(8, 8));
        tableCard.setBackground(UITheme.BG_CARD);
        tableCard.setBorder(UITheme.createCardBorder());

        JLabel tableTitle = new JLabel("INCIDENT & ANOMALY LOG HISTORY");
        tableTitle.setFont(UITheme.FONT_SUBHEADER);
        tableTitle.setForeground(UITheme.TEXT_MUTED);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Timestamp", "Severity", "Rule Type", "Alert Message", "Measured Value", "Threshold Limit"};
        alertTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        alertTable = new JTable(alertTableModel);
        UITheme.styleTable(alertTable);

        // Custom renderer for Severity column
        alertTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String val = String.valueOf(value);
                if ("CRITICAL".equals(val)) {
                    c.setForeground(UITheme.ACCENT_ROSE);
                } else if ("WARNING".equals(val)) {
                    c.setForeground(UITheme.ACCENT_AMBER);
                } else {
                    c.setForeground(UITheme.ACCENT_CYAN);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(alertTable);
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        tableCard.add(scroll, BorderLayout.CENTER);

        mainGrid.add(tableCard, BorderLayout.CENTER);
        add(mainGrid, BorderLayout.CENTER);
    }

    private void saveSettings() {
        double speed = (Double) speedSpinner.getValue();
        double quota = (Double) quotaSpinner.getValue();
        alertService.setSpeedLimitMBps(speed);
        alertService.setSessionQuotaMB(quota);
        alertService.setSpikeDetectionEnabled(spikeCheck.isSelected());
        alertService.setAudioAlertsEnabled(audioCheck.isSelected());
        JOptionPane.showMessageDialog(this, "Threshold parameters saved successfully.", "Alerts Config", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearAlerts() {
        alertService.clearAlerts();
        alertTableModel.setRowCount(0);
    }

    private void loadAlertsFromDb() {
        if (dbService != null) {
            List<AlertRecord> dbAlerts = dbService.getRecentAlerts(50);
            for (int i = dbAlerts.size() - 1; i >= 0; i--) {
                addAlertToTable(dbAlerts.get(i));
            }
        }
    }

    private void addAlertToTable(AlertRecord alert) {
        alertTableModel.insertRow(0, new Object[]{
                alert.getFormattedTimestamp(),
                alert.getLevel().name(),
                alert.getRuleType(),
                alert.getMessage(),
                String.format("%.2f", alert.getValue()),
                String.format("%.2f", alert.getThreshold())
        });
    }

    private void onAlertTriggered(AlertRecord alert) {
        SwingUtilities.invokeLater(() -> addAlertToTable(alert));
    }
}
