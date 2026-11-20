package com.netmonitor.service;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.SessionSummary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Service to generate structured network traffic and bandwidth performance reports in CSV, HTML, and Text formats.
 */
public class ReportService {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static File exportToCsv(List<BandwidthSample> samples, File destinationFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(destinationFile))) {
            writer.println("Timestamp,Formatted Date,Interface,Download (Bytes/s),Download (KB/s),Download (Mbps),Upload (Bytes/s),Upload (KB/s),Upload (Mbps),Total In (Bytes),Total Out (Bytes),Packets In,Packets Out");
            for (BandwidthSample s : samples) {
                writer.printf("%d,\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%d,%d,%d,%d%n",
                        s.getTimestamp(),
                        SDF.format(new Date(s.getTimestamp())),
                        s.getInterfaceName(),
                        s.getDownloadSpeedBps(),
                        s.getDownloadSpeedKBps(),
                        s.getDownloadMbps(),
                        s.getUploadSpeedBps(),
                        s.getUploadSpeedKBps(),
                        s.getUploadMbps(),
                        s.getTotalInBytes(),
                        s.getTotalOutBytes(),
                        s.getPacketsIn(),
                        s.getPacketsOut()
                );
            }
        }
        return destinationFile;
    }

    public static File exportToHtml(List<BandwidthSample> samples,
                                    List<AlertRecord> alerts,
                                    SessionSummary session,
                                    String interfaceName,
                                    File destinationFile) throws IOException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(destinationFile))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='en'>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>Network Bandwidth & Performance Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 24px; }");
            writer.println(".container { max-width: 1000px; margin: 0 auto; background: #1e293b; border-radius: 12px; padding: 32px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }");
            writer.println("h1, h2 { color: #38bdf8; margin-top: 0; }");
            writer.println(".badge { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: bold; }");
            writer.println(".badge-info { background: #0284c7; color: white; }");
            writer.println(".badge-warning { background: #d97706; color: white; }");
            writer.println(".badge-critical { background: #dc2626; color: white; }");
            writer.println(".grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin: 24px 0; }");
            writer.println(".card { background: #334155; padding: 18px; border-radius: 8px; border-left: 4px solid #38bdf8; }");
            writer.println(".card-title { font-size: 12px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; }");
            writer.println(".card-value { font-size: 24px; font-weight: bold; margin-top: 6px; color: #f1f5f9; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 13px; }");
            writer.println("th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid #475569; }");
            writer.println("th { background-color: #1e293b; color: #94a3b8; text-transform: uppercase; font-size: 11px; }");
            writer.println("tr:hover { background-color: #334155; }");
            writer.println(".footer { margin-top: 32px; font-size: 12px; color: #64748b; text-align: center; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<div class='container'>");

            writer.println("<h1>Network Bandwidth Monitoring & Analysis Report</h1>");
            writer.println("<p style='color: #94a3b8;'>Generated on " + SDF.format(new Date()) + " | Active Interface: <strong>" + interfaceName + "</strong></p>");

            // Summary KPI Cards
            writer.println("<div class='grid'>");
            if (session != null) {
                writer.println("<div class='card'><div class='card-title'>Total Transferred</div><div class='card-value'>" +
                        BandwidthSample.formatBytes(session.getTotalBytesIn() + session.getTotalBytesOut()) + "</div></div>");
                writer.println("<div class='card' style='border-color: #10b981;'><div class='card-title'>Total Received (DL)</div><div class='card-value'>" +
                        BandwidthSample.formatBytes(session.getTotalBytesIn()) + "</div></div>");
                writer.println("<div class='card' style='border-color: #f59e0b;'><div class='card-title'>Total Sent (UL)</div><div class='card-value'>" +
                        BandwidthSample.formatBytes(session.getTotalBytesOut()) + "</div></div>");
                writer.println("<div class='card'><div class='card-title'>Peak Download</div><div class='card-value'>" +
                        BandwidthSample.formatSpeed(session.getPeakDownSpeedBps()) + "</div></div>");
                writer.println("<div class='card'><div class='card-title'>Avg Download</div><div class='card-value'>" +
                        BandwidthSample.formatSpeed(session.getAvgDownSpeedBps()) + "</div></div>");
                writer.println("<div class='card'><div class='card-title'>Session Duration</div><div class='card-value'>" +
                        session.getDurationSeconds() + " s</div></div>");
            }
            writer.println("</div>");

            // Alerts Section
            writer.println("<h2>Recent Network Alerts (" + (alerts != null ? alerts.size() : 0) + ")</h2>");
            if (alerts == null || alerts.isEmpty()) {
                writer.println("<p style='color: #10b981;'>No alerts triggered. Network traffic was within defined parameters.</p>");
            } else {
                writer.println("<table><thead><tr><th>Time</th><th>Severity</th><th>Rule</th><th>Message</th></tr></thead><tbody>");
                for (AlertRecord a : alerts) {
                    String badgeClass = a.getLevel() == AlertRecord.Level.CRITICAL ? "badge-critical" :
                                       (a.getLevel() == AlertRecord.Level.WARNING ? "badge-warning" : "badge-info");
                    writer.printf("<tr><td>%s</td><td><span class='badge %s'>%s</span></td><td>%s</td><td>%s</td></tr>%n",
                            a.getFormattedTimestamp(), badgeClass, a.getLevel(), a.getRuleType(), a.getMessage());
                }
                writer.println("</tbody></table>");
            }

            // Recent Samples Table
            writer.println("<h2>Recent Bandwidth Snapshots</h2>");
            writer.println("<table><thead><tr><th>Time</th><th>Interface</th><th>Download Speed</th><th>Upload Speed</th><th>Total In</th><th>Total Out</th></tr></thead><tbody>");
            int count = 0;
            for (BandwidthSample s : samples) {
                if (count++ > 50) break; // limit to 50 rows in HTML
                writer.printf("<tr><td>%s</td><td>%s</td><td style='color: #38bdf8;'>%s</td><td style='color: #10b981;'>%s</td><td>%s</td><td>%s</td></tr>%n",
                        SDF.format(new Date(s.getTimestamp())),
                        s.getInterfaceName(),
                        s.getFormattedDownloadSpeed(),
                        s.getFormattedUploadSpeed(),
                        BandwidthSample.formatBytes(s.getTotalInBytes()),
                        BandwidthSample.formatBytes(s.getTotalOutBytes()));
            }
            writer.println("</tbody></table>");

            writer.println("<div class='footer'>Java Computer Networks Project &bull; Network Bandwidth Monitoring and Analysis System</div>");
            writer.println("</div></body></html>");
        }
        return destinationFile;
    }
}
