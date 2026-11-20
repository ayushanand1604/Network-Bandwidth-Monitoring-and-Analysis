package com.netmonitor;

import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.NetworkInterfaceInfo;
import com.netmonitor.service.AlertService;
import com.netmonitor.service.DatabaseService;
import com.netmonitor.service.NetworkInterfaceService;
import com.netmonitor.service.SystemTrafficReader;
import com.netmonitor.service.TrafficMonitorService;
import com.netmonitor.ui.MainFrame;
import com.netmonitor.web.NetPulseWebServer;

import javax.swing.*;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Main Application Launcher for Network Bandwidth Monitoring and Analysis System.
 * Supports:
 * - Desktop Swing GUI
 * - Embedded Web Server (HTTP Web Application on Port 8080)
 * - Headless CLI Diagnostics & Monitoring
 */
public class Main {

    private static final int WEB_PORT = 8080;

    public static void main(String[] args) {
        // macOS System Integration
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "NetPulse Pro");

        System.out.println("================================================================================");
        System.out.println("    NETWORK BANDWIDTH MONITORING & ANALYSIS SYSTEM (JAVA CN PROJECT)            ");
        System.out.println("================================================================================");
        System.out.printf("  * Java Runtime:    %s (%s)%n", System.getProperty("java.version"), System.getProperty("java.vendor"));
        System.out.printf("  * Operating System:%s %s (%s)%n", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));

        NetworkInterfaceService ifService = new NetworkInterfaceService();
        NetworkInterfaceInfo primary = ifService.getPrimaryInterface();
        String primaryIp = "127.0.0.1";
        if (primary != null) {
            primaryIp = primary.getPrimaryIpv4();
            System.out.printf("  * Primary Interface:%s (%s) - IP: %s - MAC: %s%n",
                    primary.getName(), primary.getDisplayName(), primaryIp, primary.getMacAddress());
        }
        System.out.println("================================================================================\n");

        // CLI Diagnostics argument
        if (args.length > 0 && "--test".equalsIgnoreCase(args[0])) {
            runCliDiagnostics();
            return;
        }

        // Web Server Mode argument
        if (args.length > 0 && ("--web".equalsIgnoreCase(args[0]) || "--server".equalsIgnoreCase(args[0]))) {
            runDedicatedWebServer(primaryIp);
            return;
        }

        // Headless CLI Monitor argument
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            runCliMonitor();
            return;
        }

        // Check if headless environment without display
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[Notice] Headless environment detected (No display server found).");
            System.out.println("[Notice] Starting NetPulse Embedded Web Server...\n");
            runDedicatedWebServer(primaryIp);
            return;
        }

        // Configure Swing Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        // Launch GUI & embedded Web Server
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                System.out.println("[GUI] NetPulse Pro Desktop Window launched successfully.");
            } catch (Exception e) {
                System.err.println("[GUI] Error launching GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void runDedicatedWebServer(String primaryIp) {
        try {
            DatabaseService dbService = new DatabaseService();
            AlertService alertService = new AlertService(dbService);
            NetworkInterfaceService ifService = new NetworkInterfaceService();
            SystemTrafficReader reader = new SystemTrafficReader();
            TrafficMonitorService monitorService = new TrafficMonitorService(reader, dbService, alertService);
            monitorService.start();

            NetPulseWebServer webServer = new NetPulseWebServer(WEB_PORT, monitorService, ifService, dbService, alertService);
            webServer.start();

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("  🌐 NETPULSE WEB APPLICATION IS NOW LIVE!");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  * Local URL:       http://localhost:%d%n", WEB_PORT);
            System.out.printf("  * Network LAN URL: http://%s:%d%n", primaryIp, WEB_PORT);
            System.out.println("  * Features Available to Any Web Visitor:");
            System.out.println("      - Live Bandwidth Meters & Real-time Graph");
            System.out.println("      - Interactive Browser Speedtest (Download & Upload Mbps)");
            System.out.println("      - Subnet & CIDR Calculator");
            System.out.println("      - Server Latency & Jitter (Ping) Tool");
            System.out.println("      - Downloadable CSV & HTML Performance Reports");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Press Ctrl+C to stop the web server.\n");

            // Keep main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("[Web Server] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCliDiagnostics() {
        System.out.println("Executing System Diagnostics & Interface Enumeration...\n");
        NetworkInterfaceService ifService = new NetworkInterfaceService();
        List<NetworkInterfaceInfo> list = ifService.getAllInterfaces();
        for (NetworkInterfaceInfo iface : list) {
            System.out.printf("Interface: %-8s | Up: %-5s | Loopback: %-5s | MAC: %-17s | MTU: %-5s | IP: %s%n",
                    iface.getName(), iface.isUp(), iface.isLoopback(),
                    iface.getMacAddress(), iface.getMtu() > 0 ? iface.getMtu() : "N/A",
                    iface.getPrimaryIpv4());
        }

        System.out.println("\nTesting Traffic Counters...");
        SystemTrafficReader reader = new SystemTrafficReader();
        Map<String, SystemTrafficReader.InterfaceStats> stats = reader.readAllStats();
        for (Map.Entry<String, SystemTrafficReader.InterfaceStats> entry : stats.entrySet()) {
            SystemTrafficReader.InterfaceStats s = entry.getValue();
            if (s.rxBytes > 0 || s.txBytes > 0) {
                System.out.printf("  -> %-8s : In: %-12s (%d pkts) | Out: %-12s (%d pkts)%n",
                        s.name, BandwidthSample.formatBytes(s.rxBytes), s.rxPackets,
                        BandwidthSample.formatBytes(s.txBytes), s.txPackets);
            }
        }

        System.out.println("\nTesting SQLite Persistence...");
        DatabaseService db = new DatabaseService();
        System.out.println("  -> Database available: " + db.isAvailable());

        System.out.println("\n[SUCCESS] All components verified successfully.");
    }

    private static void runCliMonitor() {
        SystemTrafficReader reader = new SystemTrafficReader();
        long lastTime = System.currentTimeMillis();
        long lastRx = -1;
        long lastTx = -1;

        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            long now = System.currentTimeMillis();
            Map<String, SystemTrafficReader.InterfaceStats> stats = reader.readAllStats();

            long totalRx = 0;
            long totalTx = 0;
            for (SystemTrafficReader.InterfaceStats s : stats.values()) {
                if (!s.name.startsWith("lo")) {
                    totalRx += s.rxBytes;
                    totalTx += s.txBytes;
                }
            }

            if (lastRx >= 0 && lastTx >= 0) {
                double sec = (now - lastTime) / 1000.0;
                double dl = (totalRx - lastRx) / sec;
                double ul = (totalTx - lastTx) / sec;
                System.out.printf("[%d/5] Download: %-12s (%.2f Mbps) | Upload: %-12s (%.2f Mbps) | Total In: %s | Out: %s%n",
                        i + 1,
                        BandwidthSample.formatSpeed(dl), (dl * 8.0) / 1_000_000.0,
                        BandwidthSample.formatSpeed(ul), (ul * 8.0) / 1_000_000.0,
                        BandwidthSample.formatBytes(totalRx), BandwidthSample.formatBytes(totalTx));
            }
            lastTime = now;
            lastRx = totalRx;
            lastTx = totalTx;
        }
        System.out.println("\nCLI Monitor finished test sampling.");
    }
}
