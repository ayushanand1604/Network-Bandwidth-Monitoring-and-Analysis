package com.netmonitor.ui;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.SessionSummary;
import com.netmonitor.service.DatabaseService;
import com.netmonitor.service.ReportService;
import com.netmonitor.service.TrafficMonitorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Historical Data Analysis & Report Generation powered by JDBC + SQLite.
 * Provides historical queries, daily usage summaries, and CSV / HTML report exports.
 */
public class HistoryReportsPanel extends JPanel {

    private final DatabaseService dbService;
    private final TrafficMonitorService monitorService;

    private DefaultTableModel samplesTableModel;
    private DefaultTableModel dailyTableModel;
    private JLabel dbStatusLabel;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HistoryReportsPanel(DatabaseService dbService, TrafficMonitorService monitorService) {
        this.dbService = dbService;
        this.monitorService = monitorService;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(UITheme.createPaddedBorder(16, 16, 16, 16));

        initHeader();
        initContent();
        refreshData();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBox.setOpaque(false);
        JLabel title = new JLabel("Database History & Network Performance Reports (SQLite + JDBC)");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        leftBox.add(title);

        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBox.setOpaque(false);

        JButton exportCsvBtn = UITheme.createStyledButton("Export to CSV", UITheme.ACCENT_CYAN);
        exportCsvBtn.addActionListener(e -> exportCsv());
        actionBox.add(exportCsvBtn);

        JButton exportHtmlBtn = UITheme.createStyledButton("Export HTML Report", UITheme.ACCENT_EMERALD);
        exportHtmlBtn.addActionListener(e -> exportHtml());
        actionBox.add(exportHtmlBtn);

        JButton refreshBtn = UITheme.createStyledButton("Refresh", UITheme.ACCENT_PURPLE);
        refreshBtn.addActionListener(e -> refreshData());
        actionBox.add(refreshBtn);

        JButton clearBtn = UITheme.createStyledButton("Purge DB", UITheme.ACCENT_ROSE);
        clearBtn.addActionListener(e -> purgeDatabase());
        actionBox.add(clearBtn);

        header.add(leftBox, BorderLayout.WEST);
        header.add(actionBox, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setDividerLocation(360);
        split.setBorder(null);

        // Left: Daily Usage Rollup
        JPanel leftCard = new JPanel(new BorderLayout(8, 8));
        leftCard.setBackground(UITheme.BG_CARD);
        leftCard.setBorder(UITheme.createCardBorder());

        JLabel leftTitle = new JLabel("DAILY USAGE SUMMARY (SQL ROLLUP)");
        leftTitle.setFont(UITheme.FONT_SUBHEADER);
        leftTitle.setForeground(UITheme.TEXT_MUTED);
        leftCard.add(leftTitle, BorderLayout.NORTH);

        String[] dailyCols = {"Date", "Peak DL Speed", "Recorded Samples"};
        dailyTableModel = new DefaultTableModel(dailyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable dailyTable = new JTable(dailyTableModel);
        UITheme.styleTable(dailyTable);
        JScrollPane dailyScroll = new JScrollPane(dailyTable);
        dailyScroll.getViewport().setBackground(UITheme.BG_CARD);
        dailyScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        leftCard.add(dailyScroll, BorderLayout.CENTER);

        split.setLeftComponent(leftCard);

        // Right: Recent Bandwidth Snapshots
        JPanel rightCard = new JPanel(new BorderLayout(8, 8));
        rightCard.setBackground(UITheme.BG_CARD);
        rightCard.setBorder(UITheme.createCardBorder());

        JLabel rightTitle = new JLabel("RECORDED BANDWIDTH SNAPSHOTS (network_monitor.db)");
        rightTitle.setFont(UITheme.FONT_SUBHEADER);
        rightTitle.setForeground(UITheme.TEXT_MUTED);
        rightCard.add(rightTitle, BorderLayout.NORTH);

        String[] sampleCols = {"Timestamp", "Interface", "Download Speed", "Upload Speed", "Total In", "Total Out"};
        samplesTableModel = new DefaultTableModel(sampleCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable samplesTable = new JTable(samplesTableModel);
        UITheme.styleTable(samplesTable);
        JScrollPane sampleScroll = new JScrollPane(samplesTable);
        sampleScroll.getViewport().setBackground(UITheme.BG_CARD);
        sampleScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        rightCard.add(sampleScroll, BorderLayout.CENTER);

        // Footer status
        dbStatusLabel = new JLabel("SQLite Database: Active (network_monitor.db)");
        dbStatusLabel.setFont(UITheme.FONT_SMALL);
        dbStatusLabel.setForeground(UITheme.ACCENT_EMERALD);
        rightCard.add(dbStatusLabel, BorderLayout.SOUTH);

        split.setRightComponent(rightCard);

        add(split, BorderLayout.CENTER);
    }

    private void refreshData() {
        if (dbService == null || !dbService.isAvailable()) {
            dbStatusLabel.setText("SQLite Database: Inactive (Using Memory Cache)");
            dbStatusLabel.setForeground(UITheme.ACCENT_AMBER);
            return;
        }

        dbStatusLabel.setText("SQLite Database: Active & Connected (network_monitor.db)");
        dbStatusLabel.setForeground(UITheme.ACCENT_EMERALD);

        // Refresh Daily
        dailyTableModel.setRowCount(0);
        List<DatabaseService.DailyUsage> dailyList = dbService.getDailyUsageStats();
        for (DatabaseService.DailyUsage du : dailyList) {
            dailyTableModel.addRow(new Object[]{
                    du.date,
                    BandwidthSample.formatSpeed(du.peakDownSpeedBps),
                    du.sessionCount
            });
        }

        // Refresh Samples
        samplesTableModel.setRowCount(0);
        List<BandwidthSample> samples = dbService.getRecentSamples(100);
        for (BandwidthSample s : samples) {
            samplesTableModel.addRow(new Object[]{
                    SDF.format(new Date(s.getTimestamp())),
                    s.getInterfaceName(),
                    s.getFormattedDownloadSpeed(),
                    s.getFormattedUploadSpeed(),
                    BandwidthSample.formatBytes(s.getTotalInBytes()),
                    BandwidthSample.formatBytes(s.getTotalOutBytes())
            });
        }
    }

    private void purgeDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to purge all stored network records?",
                "Purge Database", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dbService.clearHistory();
            refreshData();
            JOptionPane.showMessageDialog(this, "Historical records cleared successfully.");
        }
    }

    private void exportCsv() {
        List<BandwidthSample> samples = dbService.getRecentSamples(1000);
        if (samples.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records available to export.", "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("network_traffic_report.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                ReportService.exportToCsv(samples, file);
                JOptionPane.showMessageDialog(this, "CSV Report exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportHtml() {
        List<BandwidthSample> samples = dbService.getRecentSamples(100);
        List<AlertRecord> alerts = dbService.getRecentAlerts(20);
        SessionSummary session = monitorService.getCurrentSession();
        String ifName = monitorService.getTargetInterface() != null ? monitorService.getTargetInterface() : "Active Interface";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("network_performance_report.html"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                ReportService.exportToHtml(samples, alerts, session, ifName, file);
                int openChoice = JOptionPane.showConfirmDialog(this,
                        "HTML Performance Report generated successfully!\nWould you like to open it in your browser now?",
                        "Report Created", JOptionPane.YES_NO_OPTION);
                if (openChoice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export HTML report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
