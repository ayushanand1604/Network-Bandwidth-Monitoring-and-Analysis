package com.netmonitor.service;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.SessionSummary;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Persistence service using JDBC + SQLite.
 * Stores bandwidth samples, session aggregates, and alert logs.
 */
public class DatabaseService {

    private static final String DB_NAME = "network_monitor.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    private boolean isAvailable = false;

    public DatabaseService() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                // Bandwidth snapshots table
                stmt.execute("CREATE TABLE IF NOT EXISTS bandwidth_samples (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "timestamp INTEGER NOT NULL, " +
                        "interface_name TEXT NOT NULL, " +
                        "download_bps REAL NOT NULL, " +
                        "upload_bps REAL NOT NULL, " +
                        "total_in INTEGER NOT NULL, " +
                        "total_out INTEGER NOT NULL, " +
                        "packets_in INTEGER NOT NULL, " +
                        "packets_out INTEGER NOT NULL" +
                        ")");

                // Monitoring sessions summary table
                stmt.execute("CREATE TABLE IF NOT EXISTS monitoring_sessions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "start_time INTEGER NOT NULL, " +
                        "end_time INTEGER NOT NULL, " +
                        "interface_name TEXT NOT NULL, " +
                        "total_in INTEGER NOT NULL, " +
                        "total_out INTEGER NOT NULL, " +
                        "peak_down_bps REAL NOT NULL, " +
                        "peak_up_bps REAL NOT NULL, " +
                        "avg_down_bps REAL NOT NULL, " +
                        "avg_up_bps REAL NOT NULL" +
                        ")");

                // Alerts table
                stmt.execute("CREATE TABLE IF NOT EXISTS alerts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "timestamp INTEGER NOT NULL, " +
                        "level TEXT NOT NULL, " +
                        "rule_type TEXT NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "value REAL NOT NULL, " +
                        "threshold REAL NOT NULL" +
                        ")");

                isAvailable = true;
            }
        } catch (Exception e) {
            System.err.println("[DatabaseService] Warning: SQLite initialization fallback: " + e.getMessage());
            isAvailable = false;
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public synchronized void saveSample(BandwidthSample sample) {
        if (!isAvailable) return;
        String sql = "INSERT INTO bandwidth_samples (timestamp, interface_name, download_bps, upload_bps, " +
                     "total_in, total_out, packets_in, packets_out) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sample.getTimestamp());
            ps.setString(2, sample.getInterfaceName());
            ps.setDouble(3, sample.getDownloadSpeedBps());
            ps.setDouble(4, sample.getUploadSpeedBps());
            ps.setLong(5, sample.getTotalInBytes());
            ps.setLong(6, sample.getTotalOutBytes());
            ps.setLong(7, sample.getPacketsIn());
            ps.setLong(8, sample.getPacketsOut());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error saving sample: " + e.getMessage());
        }
    }

    public synchronized void saveSession(SessionSummary session, String interfaceName) {
        if (!isAvailable || session == null) return;
        String sql = "INSERT INTO monitoring_sessions (start_time, end_time, interface_name, " +
                     "total_in, total_out, peak_down_bps, peak_up_bps, avg_down_bps, avg_up_bps) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, session.getStartTime());
            ps.setLong(2, session.getEndTime());
            ps.setString(3, interfaceName);
            ps.setLong(4, session.getTotalBytesIn());
            ps.setLong(5, session.getTotalBytesOut());
            ps.setDouble(6, session.getPeakDownSpeedBps());
            ps.setDouble(7, session.getPeakUpSpeedBps());
            ps.setDouble(8, session.getAvgDownSpeedBps());
            ps.setDouble(9, session.getAvgUpSpeedBps());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error saving session: " + e.getMessage());
        }
    }

    public synchronized void saveAlert(AlertRecord alert) {
        if (!isAvailable || alert == null) return;
        String sql = "INSERT INTO alerts (timestamp, level, rule_type, message, value, threshold) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, alert.getTimestamp());
            ps.setString(2, alert.getLevel().name());
            ps.setString(3, alert.getRuleType());
            ps.setString(4, alert.getMessage());
            ps.setDouble(5, alert.getValue());
            ps.setDouble(6, alert.getThreshold());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error saving alert: " + e.getMessage());
        }
    }

    public synchronized List<BandwidthSample> getRecentSamples(int limit) {
        List<BandwidthSample> list = new ArrayList<>();
        if (!isAvailable) return list;
        String sql = "SELECT * FROM bandwidth_samples ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BandwidthSample(
                            rs.getLong("timestamp"),
                            rs.getString("interface_name"),
                            rs.getDouble("download_bps"),
                            rs.getDouble("upload_bps"),
                            rs.getLong("total_in"),
                            rs.getLong("total_out"),
                            rs.getLong("packets_in"),
                            rs.getLong("packets_out")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error getting samples: " + e.getMessage());
        }
        return list;
    }

    public synchronized List<AlertRecord> getRecentAlerts(int limit) {
        List<AlertRecord> list = new ArrayList<>();
        if (!isAvailable) return list;
        String sql = "SELECT * FROM alerts ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AlertRecord(
                            rs.getLong("timestamp"),
                            AlertRecord.Level.valueOf(rs.getString("level")),
                            rs.getString("rule_type"),
                            rs.getString("message"),
                            rs.getDouble("value"),
                            rs.getDouble("threshold")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error getting alerts: " + e.getMessage());
        }
        return list;
    }

    public static class DailyUsage {
        public String date;
        public long totalDownloadBytes;
        public long totalUploadBytes;
        public double peakDownSpeedBps;
        public int sessionCount;
    }

    public synchronized List<DailyUsage> getDailyUsageStats() {
        List<DailyUsage> result = new ArrayList<>();
        if (!isAvailable) return result;
        // Group by Date string using SQLite strftime
        String sql = "SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch', 'localtime') as day, " +
                     "MAX(download_bps) as peak_dl, " +
                     "COUNT(*) as sample_count " +
                     "FROM bandwidth_samples GROUP BY day ORDER BY day DESC LIMIT 30";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DailyUsage u = new DailyUsage();
                u.date = rs.getString("day");
                u.peakDownSpeedBps = rs.getDouble("peak_dl");
                u.sessionCount = rs.getInt("sample_count");
                result.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error getting daily usage: " + e.getMessage());
        }
        return result;
    }

    public synchronized void clearHistory() {
        if (!isAvailable) return;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM bandwidth_samples");
            stmt.execute("DELETE FROM monitoring_sessions");
            stmt.execute("DELETE FROM alerts");
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error clearing history: " + e.getMessage());
        }
    }
}
