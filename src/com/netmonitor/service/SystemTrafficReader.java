package com.netmonitor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-platform network traffic statistics reader.
 * Interrogates OS network subsystems:
 * - macOS / BSD: netstat -ibn
 * - Linux: /proc/net/dev or netstat -i
 * - Windows: netstat -e / netsh
 */
public class SystemTrafficReader {

    public static class InterfaceStats {
        public String name;
        public long rxBytes;
        public long txBytes;
        public long rxPackets;
        public long txPackets;

        public InterfaceStats(String name, long rxBytes, long txBytes, long rxPackets, long txPackets) {
            this.name = name;
            this.rxBytes = Math.max(0, rxBytes);
            this.txBytes = Math.max(0, txBytes);
            this.rxPackets = Math.max(0, rxPackets);
            this.txPackets = Math.max(0, txPackets);
        }
    }

    private final String osName;

    public SystemTrafficReader() {
        this.osName = System.getProperty("os.name", "").toLowerCase();
    }

    /**
     * Reads current cumulative interface statistics mapped by interface name.
     */
    public Map<String, InterfaceStats> readAllStats() {
        Map<String, InterfaceStats> stats = new HashMap<>();
        try {
            if (osName.contains("mac") || osName.contains("darwin") || osName.contains("bsd")) {
                stats = readMacStats();
            } else if (osName.contains("linux")) {
                stats = readLinuxStats();
            } else if (osName.contains("win")) {
                stats = readWindowsStats();
            } else {
                // Fallback attempt macOS/Linux netstat
                stats = readMacStats();
            }
        } catch (Exception e) {
            // Silently fall back to empty or cached
        }
        return stats;
    }

    /**
     * Parse macOS `netstat -ibn`
     * Headers: Name Mtu Network Address Ipkts Ierrs Ibytes Opkts Oerrs Obytes Coll
     */
    private Map<String, InterfaceStats> readMacStats() {
        Map<String, InterfaceStats> map = new HashMap<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"netstat", "-ibn"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("<Link")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 10) {
                            String name = parts[0].replaceAll("\\*$", "");
                            try {
                                long ipkts = Long.parseLong(parts[parts.length - 7]);
                                long ibytes = Long.parseLong(parts[parts.length - 5]);
                                long opkts = Long.parseLong(parts[parts.length - 4]);
                                long obytes = Long.parseLong(parts[parts.length - 2]);

                                map.put(name, new InterfaceStats(name, ibytes, obytes, ipkts, opkts));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception ignored) {}
        return map;
    }

    /**
     * Parse Linux `/proc/net/dev`
     */
    private Map<String, InterfaceStats> readLinuxStats() {
        Map<String, InterfaceStats> map = new HashMap<>();
        File procNetDev = new File("/proc/net/dev");
        if (procNetDev.exists() && procNetDev.canRead()) {
            try {
                List<String> lines = Files.readAllLines(procNetDev.toPath());
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        String name = parts[0].trim();
                        String[] data = parts[1].trim().split("\\s+");
                        if (data.length >= 16) {
                            long rxBytes = Long.parseLong(data[0]);
                            long rxPackets = Long.parseLong(data[1]);
                            long txBytes = Long.parseLong(data[8]);
                            long txPackets = Long.parseLong(data[9]);
                            map.put(name, new InterfaceStats(name, rxBytes, txBytes, rxPackets, txPackets));
                        }
                    }
                }
                return map;
            } catch (Exception ignored) {}
        }
        // Fallback to netstat -i
        return readMacStats();
    }

    /**
     * Parse Windows netstat -e
     */
    private Map<String, InterfaceStats> readWindowsStats() {
        Map<String, InterfaceStats> map = new HashMap<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"netstat", "-e"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                long bytesIn = 0, bytesOut = 0, pktsIn = 0, pktsOut = 0;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("Bytes")) {
                        String[] parts = trimmed.split("\\s+");
                        if (parts.length >= 3) {
                            bytesIn = Long.parseLong(parts[1]);
                            bytesOut = Long.parseLong(parts[2]);
                        }
                    } else if (trimmed.startsWith("Unicast packets") || trimmed.startsWith("Packets")) {
                        String[] parts = trimmed.split("\\s+");
                        if (parts.length >= 3) {
                            pktsIn = Long.parseLong(parts[parts.length - 2]);
                            pktsOut = Long.parseLong(parts[parts.length - 1]);
                        }
                    }
                }
                map.put("all", new InterfaceStats("all", bytesIn, bytesOut, pktsIn, pktsOut));
            }
            process.waitFor();
        } catch (Exception ignored) {}
        return map;
    }
}
