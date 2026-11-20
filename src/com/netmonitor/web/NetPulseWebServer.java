package com.netmonitor.web;

import com.netmonitor.model.AlertRecord;
import com.netmonitor.model.BandwidthSample;
import com.netmonitor.model.NetworkInterfaceInfo;
import com.netmonitor.model.SessionSummary;
import com.netmonitor.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Embedded Lightweight Java HTTP Web Server.
 * Serves the NetPulse Web Application and provides REST APIs for:
 * - Real-time bandwidth streaming
 * - Client browser speed testing (Download & Upload Mbps)
 * - Network interface & Subnet inspection
 * - Latency & Jitter (Ping) probes
 * - SQLite history queries & CSV/HTML report downloads
 */
public class NetPulseWebServer {

    private final int port;
    private final TrafficMonitorService monitorService;
    private final NetworkInterfaceService ifService;
    private final DatabaseService dbService;
    private final AlertService alertService;
    private final PingService pingService;

    private HttpServer server;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public NetPulseWebServer(int port, TrafficMonitorService monitorService,
                            NetworkInterfaceService ifService, DatabaseService dbService,
                            AlertService alertService) {
        this.port = port;
        this.monitorService = monitorService;
        this.ifService = ifService;
        this.dbService = dbService;
        this.alertService = alertService;
        this.pingService = new PingService();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public int getPort() {
        return port;
    }

    public synchronized void start() throws IOException {
        if (isRunning.get()) return;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // Static routes & Web App UI
        server.createContext("/", new WebAppHandler());

        // API routes
        server.createContext("/api/stats", new StatsApiHandler());
        server.createContext("/api/interfaces", new InterfacesApiHandler());
        server.createContext("/api/ping", new PingApiHandler());
        server.createContext("/api/history", new HistoryApiHandler());
        server.createContext("/api/report/csv", new ReportCsvHandler());
        server.createContext("/api/report/html", new ReportHtmlHandler());

        // Speedtest endpoints for browser clients
        server.createContext("/api/speedtest/ping", new SpeedtestPingHandler());
        server.createContext("/api/speedtest/download", new SpeedtestDownloadHandler());
        server.createContext("/api/speedtest/upload", new SpeedtestUploadHandler());

        server.start();
        isRunning.set(true);
        System.out.println("[Web Server] NetPulse Web Application online at: http://localhost:" + port);
    }

    public synchronized void stop() {
        if (!isRunning.get()) return;
        isRunning.set(false);
        if (server != null) {
            server.stop(0);
            server = null;
        }
        System.out.println("[Web Server] NetPulse Web Application stopped.");
    }

    // =========================================================================
    // Handlers
    // =========================================================================

    private class WebAppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/") && !path.equals("/index.html")) {
                sendResponse(exchange, 404, "text/plain", "Not Found");
                return;
            }
            String html = WebPageTemplate.getHtml(port);
            sendResponse(exchange, 200, "text/html; charset=UTF-8", html);
        }
    }

    private class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            SessionSummary session = monitorService.getCurrentSession();
            List<BandwidthSample> recent = dbService.getRecentSamples(1);
            BandwidthSample sample = recent.isEmpty() ? null : recent.get(0);

            double dlBps = sample != null ? sample.getDownloadSpeedBps() : 0;
            double ulBps = sample != null ? sample.getUploadSpeedBps() : 0;
            long totalIn = session != null ? session.getTotalBytesIn() : (sample != null ? sample.getTotalInBytes() : 0);
            long totalOut = session != null ? session.getTotalBytesOut() : (sample != null ? sample.getTotalOutBytes() : 0);
            double peakDl = session != null ? session.getPeakDownSpeedBps() : dlBps;
            double peakUl = session != null ? session.getPeakUpSpeedBps() : ulBps;
            long pktsIn = sample != null ? sample.getPacketsIn() : 0;
            long pktsOut = sample != null ? sample.getPacketsOut() : 0;
            String ifName = monitorService.getTargetInterface() != null ? monitorService.getTargetInterface() : "Active";

            String json = String.format(Locale.US,
                    "{\"timestamp\":%d,\"interface\":\"%s\",\"downloadBps\":%.2f,\"uploadBps\":%.2f," +
                    "\"downloadMbps\":%.2f,\"uploadMbps\":%.2f,\"totalIn\":%d,\"totalOut\":%d," +
                    "\"peakDownBps\":%.2f,\"peakUpBps\":%.2f,\"packetsIn\":%d,\"packetsOut\":%d}",
                    System.currentTimeMillis(), ifName, dlBps, ulBps,
                    (dlBps * 8.0) / 1_000_000.0, (ulBps * 8.0) / 1_000_000.0,
                    totalIn, totalOut, peakDl, peakUl, pktsIn, pktsOut);

            sendResponse(exchange, 200, "application/json", json);
        }
    }

    private class InterfacesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            List<NetworkInterfaceInfo> list = ifService.getAllInterfaces();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                NetworkInterfaceInfo ni = list.get(i);
                if (i > 0) sb.append(",");
                String sub = ni.getSubnetMasks().isEmpty() ? "N/A" : ni.getSubnetMasks().get(0);
                sb.append(String.format("{\"name\":\"%s\",\"displayName\":\"%s\",\"mac\":\"%s\",\"mtu\":%d," +
                                "\"isUp\":%b,\"isLoopback\":%b,\"ipv4\":\"%s\",\"subnet\":\"%s\"}",
                        escapeJson(ni.getName()), escapeJson(ni.getDisplayName()),
                        escapeJson(ni.getMacAddress()), ni.getMtu(), ni.isUp(), ni.isLoopback(),
                        escapeJson(ni.getPrimaryIpv4()), escapeJson(sub)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class PingApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String host = params.getOrDefault("host", "8.8.8.8");
            int count = 4;
            try {
                count = Integer.parseInt(params.getOrDefault("count", "4"));
            } catch (Exception ignored) {}

            final Object lock = new Object();
            final PingService.PingStatistics[] resultHolder = new PingService.PingStatistics[1];

            pingService.executePingAsync(host, count, 2000, new PingService.PingCallback() {
                @Override
                public void onPingResult(com.netmonitor.model.PingResult result) {}

                @Override
                public void onPingComplete(PingService.PingStatistics stats) {
                    synchronized (lock) {
                        resultHolder[0] = stats;
                        lock.notifyAll();
                    }
                }
            });

            synchronized (lock) {
                try {
                    lock.wait(10000);
                } catch (InterruptedException ignored) {}
            }

            PingService.PingStatistics stats = resultHolder[0];
            String json;
            if (stats != null) {
                json = String.format(Locale.US,
                        "{\"host\":\"%s\",\"transmitted\":%d,\"received\":%d,\"lost\":%d," +
                        "\"lossRatePercent\":%.1f,\"minRtt\":%d,\"maxRtt\":%d,\"avgRtt\":%.1f,\"jitter\":%.2f}",
                        escapeJson(stats.host), stats.transmitted, stats.received, stats.lost,
                        stats.lossRatePercent, stats.minRtt, stats.maxRtt, stats.avgRtt, stats.jitter);
            } else {
                json = "{\"error\":\"Ping timeout or failed\"}";
            }
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    private class HistoryApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            List<BandwidthSample> samples = dbService.getRecentSamples(30);
            List<AlertRecord> alerts = dbService.getRecentAlerts(15);
            List<DatabaseService.DailyUsage> daily = dbService.getDailyUsageStats();

            StringBuilder sb = new StringBuilder("{");
            // Daily
            sb.append("\"daily\":[");
            for (int i = 0; i < daily.size(); i++) {
                if (i > 0) sb.append(",");
                DatabaseService.DailyUsage d = daily.get(i);
                sb.append(String.format("{\"date\":\"%s\",\"peakDownBps\":%.2f,\"count\":%d}",
                        d.date, d.peakDownSpeedBps, d.sessionCount));
            }
            sb.append("],");

            // Alerts
            sb.append("\"alerts\":[");
            for (int i = 0; i < alerts.size(); i++) {
                if (i > 0) sb.append(",");
                AlertRecord a = alerts.get(i);
                sb.append(String.format("{\"time\":\"%s\",\"level\":\"%s\",\"rule\":\"%s\",\"msg\":\"%s\"}",
                        a.getFormattedTimestamp(), a.getLevel().name(), escapeJson(a.getRuleType()), escapeJson(a.getMessage())));
            }
            sb.append("],");

            // Samples
            sb.append("\"samples\":[");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < samples.size(); i++) {
                if (i > 0) sb.append(",");
                BandwidthSample s = samples.get(i);
                sb.append(String.format(Locale.US, "{\"time\":\"%s\",\"iface\":\"%s\",\"dlBps\":%.2f,\"ulBps\":%.2f,\"inBytes\":%d,\"outBytes\":%d}",
                        sdf.format(new Date(s.getTimestamp())), escapeJson(s.getInterfaceName()),
                        s.getDownloadSpeedBps(), s.getUploadSpeedBps(), s.getTotalInBytes(), s.getTotalOutBytes()));
            }
            sb.append("]}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class SpeedtestPingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String json = "{\"status\":\"ok\",\"timestamp\":" + System.currentTimeMillis() + "}";
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    private class SpeedtestDownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int sizeBytes = 5 * 1024 * 1024; // Default 5 MB
            try {
                if (params.containsKey("size")) {
                    sizeBytes = Integer.parseInt(params.get("size"));
                }
            } catch (Exception ignored) {}

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, sizeBytes);

            byte[] buffer = new byte[64 * 1024];
            // Fill with deterministic pattern
            for (int i = 0; i < buffer.length; i++) buffer[i] = (byte) (i % 128);

            try (OutputStream os = exchange.getResponseBody()) {
                int remaining = sizeBytes;
                while (remaining > 0) {
                    int toWrite = Math.min(buffer.length, remaining);
                    os.write(buffer, 0, toWrite);
                    remaining -= toWrite;
                }
                os.flush();
            }
        }
    }

    private class SpeedtestUploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            long startTime = System.currentTimeMillis();
            long totalRead = 0;
            byte[] buf = new byte[64 * 1024];
            try (InputStream is = exchange.getRequestBody()) {
                int r;
                while ((r = is.read(buf)) != -1) {
                    totalRead += r;
                }
            }
            long duration = Math.max(1, System.currentTimeMillis() - startTime);
            double mbps = (totalRead * 8.0) / (duration * 1000.0);

            String json = String.format(Locale.US,
                    "{\"bytesReceived\":%d,\"durationMs\":%d,\"throughputMbps\":%.2f}",
                    totalRead, duration, mbps);
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    private class ReportCsvHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<BandwidthSample> samples = dbService.getRecentSamples(1000);
            File temp = File.createTempFile("network_report", ".csv");
            ReportService.exportToCsv(samples, temp);

            exchange.getResponseHeaders().set("Content-Type", "text/csv");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"network_traffic_report.csv\"");
            exchange.sendResponseHeaders(200, temp.length());

            try (FileInputStream fis = new FileInputStream(temp);
                 OutputStream os = exchange.getResponseBody()) {
                fis.transferTo(os);
            }
            temp.delete();
        }
    }

    private class ReportHtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<BandwidthSample> samples = dbService.getRecentSamples(100);
            List<AlertRecord> alerts = dbService.getRecentAlerts(20);
            SessionSummary session = monitorService.getCurrentSession();
            String ifName = monitorService.getTargetInterface() != null ? monitorService.getTargetInterface() : "Active";

            File temp = File.createTempFile("network_report", ".html");
            ReportService.exportToHtml(samples, alerts, session, ifName, temp);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"network_performance_report.html\"");
            exchange.sendResponseHeaders(200, temp.length());

            try (FileInputStream fis = new FileInputStream(temp);
                 OutputStream os = exchange.getResponseBody()) {
                fis.transferTo(os);
            }
            temp.delete();
        }
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] entry = param.split("=", 2);
            if (entry.length > 1) {
                map.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
