package com.netmonitor.ui;

import com.netmonitor.service.*;
import com.netmonitor.web.NetPulseWebServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

/**
 * Main Application Window housing the tabbed interface, application lifecycle,
 * desktop UI components, and embedded web server.
 */
public class MainFrame extends JFrame {

    private final TrafficMonitorService monitorService;
    private final NetworkInterfaceService ifService;
    private final DatabaseService dbService;
    private final AlertService alertService;
    private NetPulseWebServer webServer;

    private CNLabPanel cnLabPanel;

    public MainFrame() {
        super("Network Bandwidth Monitoring & Analysis System (Computer Networks Project)");

        // Initialize Services
        this.dbService = new DatabaseService();
        this.alertService = new AlertService(dbService);
        this.ifService = new NetworkInterfaceService();
        SystemTrafficReader trafficReader = new SystemTrafficReader();
        this.monitorService = new TrafficMonitorService(trafficReader, dbService, alertService);

        initUi();

        // Start background traffic polling
        monitorService.start();

        // Start embedded Web Server on port 8080
        try {
            webServer = new NetPulseWebServer(8080, monitorService, ifService, dbService, alertService);
            webServer.start();
        } catch (Exception e) {
            System.err.println("[MainFrame] Warning: Could not start web server on 8080: " + e.getMessage());
        }

        // Graceful cleanup on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 840);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        // App Banner / Header
        JPanel appHeader = new JPanel(new BorderLayout());
        appHeader.setBackground(new Color(11, 17, 33));
        appHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel logoIcon = new JLabel("⚡");
        logoIcon.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoIcon.setForeground(UITheme.ACCENT_CYAN);

        JLabel appTitle = new JLabel("NetPulse Pro");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        appTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel appSubtitle = new JLabel("| Network Bandwidth Monitoring & Performance Analyzer");
        appSubtitle.setFont(UITheme.FONT_BODY);
        appSubtitle.setForeground(UITheme.TEXT_MUTED);

        titlePanel.add(logoIcon);
        titlePanel.add(appTitle);
        titlePanel.add(appSubtitle);

        // Right side: Web UI button & Badge
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JButton openWebBtn = UITheme.createStyledButton("🌐 Open Web UI (Port 8080)", UITheme.ACCENT_CYAN);
        openWebBtn.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                } else {
                    JOptionPane.showMessageDialog(this, "Open your browser to: http://localhost:8080", "Web UI", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open browser: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        rightBox.add(openWebBtn);

        JLabel badge = new JLabel("JAVA SE 24 • WEB & DESKTOP");
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(UITheme.ACCENT_EMERALD);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_EMERALD, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        rightBox.add(badge);

        appHeader.add(titlePanel, BorderLayout.WEST);
        appHeader.add(rightBox, BorderLayout.EAST);

        add(appHeader, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_HEADER);
        tabbedPane.setBackground(UITheme.BG_DARK);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);

        // Tabs
        DashboardPanel dashboardPanel = new DashboardPanel(monitorService, ifService, alertService);
        TrafficAnalysisPanel analysisPanel = new TrafficAnalysisPanel(monitorService);
        InterfacePanel interfacePanel = new InterfacePanel(ifService);
        cnLabPanel = new CNLabPanel();
        AlertsPanel alertsPanel = new AlertsPanel(alertService, dbService);
        HistoryReportsPanel reportsPanel = new HistoryReportsPanel(dbService, monitorService);

        tabbedPane.addTab("  Live Dashboard  ", dashboardPanel);
        tabbedPane.addTab("  Traffic Analysis  ", analysisPanel);
        tabbedPane.addTab("  Interfaces & Subnets  ", interfacePanel);
        tabbedPane.addTab("  CN Lab & Benchmarks  ", cnLabPanel);
        tabbedPane.addTab("  Alerts & Thresholds  ", alertsPanel);
        tabbedPane.addTab("  History & Reports  ", reportsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void shutdown() {
        if (monitorService != null) {
            monitorService.stop();
        }
        if (cnLabPanel != null) {
            cnLabPanel.cleanup();
        }
        if (webServer != null) {
            webServer.stop();
        }
    }
}
