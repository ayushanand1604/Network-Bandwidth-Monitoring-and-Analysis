# ⚡ NetPulse Pro: Network Bandwidth Monitoring and Analysis System (Desktop & Web)

A comprehensive application built in **Java** for real-time network traffic monitoring, bandwidth analysis, latency profiling, and practical demonstration of core **Computer Networks** concepts.

Available as both a **Java Swing Desktop Application** and a **Web Application (Accessible from any browser/mobile device)**.

---

## 📋 Table of Contents
1. [Project Overview](#-project-overview)
2. [Web Application & Public Access](#-web-application--public-access)
3. [Computer Networks Concepts Demonstrated](#-computer-networks-concepts-demonstrated)
4. [Key Features](#-key-features)
5. [Technology Stack](#-technology-stack)
6. [Project Architecture & Directory Structure](#-project-architecture--directory-structure)
7. [How to Build and Run](#-how-to-build-and-run)
8. [Database Schema (SQLite + JDBC)](#-database-schema-sqlite--jdbc)
9. [Reporting Engine (HTML & CSV Exports)](#-reporting-engine-html--csv-exports)
10. [Viva & Academic Presentation Guide](#-viva--academic-presentation-guide)

---

## 🎯 1. Project Overview

**Project Title:** Network Bandwidth Monitoring and Analysis System  
**Language:** Pure Java (Java SE 17 / 21 / 24 compatible)  
**Interface:** Dual Interface:
  1. **Java Swing Desktop GUI** (Dark Dashboard with JFreeChart Visualizations)
  2. **Responsive Web Application** (Embedded HTTP Server on Port 8080)
**Database:** SQLite via JDBC  

---

## 🌐 2. Web Application & Public Access

NetPulse Pro includes an **Embedded Java HTTP Web Server** (`com.sun.net.httpserver.HttpServer`) built right into the JDK with zero external web dependencies.

### Accessing Locally (Desktop & Mobile)
Whenever you start the application, the web server is online at:
- **Local Browser:** `http://localhost:8080`
- **Any Device on Same Wi-Fi/LAN (Phone, Tablet, Laptop):** `http://<Your-Mac-IP>:8080` (e.g. `http://192.168.1.2:8080`)

### Giving Access to Anyone in the World (Public URL)
To share this application as a public website so anyone anywhere can test it:
1. **Using Free Cloudflare Tunnel (Zero Configuration / No Port Forwarding):**
   ```bash
   # Install cloudflared via Homebrew (if not already installed)
   brew install cloudflared
   
   # Start the public tunnel to port 8080
   cloudflared tunnel --url http://localhost:8080
   ```
   Cloudflare will give you an instant public HTTPS URL like:
   `https://random-subdomain.trycloudflare.com`
   Anyone across the world can open this link on their phone or PC!

2. **Using Ngrok:**
   ```bash
   ngrok http 8080
   ```
   Gives a public URL like `https://xyz.ngrok-free.app`.

### Web Features Available to Any Visitor
1. **Live Host Traffic Monitor:** Real-time auto-updating bandwidth graphs and KPI meters streaming directly from the host.
2. **Interactive Browser Speedtest:** Visitors can click "Start Speed Test" right in their browser to measure real latency, jitter, download throughput, and upload throughput.
3. **Subnet & CIDR Calculator:** Instant bitwise network address, broadcast address, and usable host calculations.
4. **Server Latency & Ping Prober:** Trigger server-side ICMP/Socket pings to remote targets.
5. **Download Reports:** One-click CSV and HTML performance report downloads directly in the browser.

---

In modern computer networking, tracking bandwidth consumption, identifying network bottlenecks, monitoring interface activity, and assessing transmission latency are essential for network administrators. This project provides an integrated desktop dashboard that continuously inspects network interfaces, monitors live upload and download throughput, detects traffic bursts/anomalies, and provides a built-in **Computer Networks Lab Suite** (Ping RTT/Jitter analyzer and TCP/UDP Client-Server throughput benchmark).

---

## 🌐 2. Computer Networks Concepts Demonstrated

This project is tailored specifically for Computer Networks courses and showcases the theoretical syllabus concepts in a running software implementation:

| Computer Networks Concept | Software Implementation in NetPulse Pro |
| :--- | :--- |
| **Bandwidth** | Real-time calculation of theoretical and actual data transfer rates in bits/sec ($\text{bps}$), $\text{Kbps}$, $\text{Mbps}$, and $\text{MB/s}$. |
| **Throughput vs Bandwidth** | Interactive benchmark calculates actual throughput: $\text{Throughput (Mbps)} = \frac{\text{Bytes Transferred} \times 8}{\text{Elapsed Seconds} \times 10^6}$. |
| **Data Transmission** | Tracks instantaneous and cumulative incoming ($\text{Rx}$) and outgoing ($\text{Tx}$) bytes and packet counters. |
| **IP Addressing & Classes** | Analyzes $\text{IPv4}$ and $\text{IPv6}$ configurations, identifies address class (Class A, B, C, D Multicast, E), loopback ($\text{127.0.0.1}$), and RFC 1918 Private vs. Public scopes. |
| **Subnetting & CIDR** | Computes Network Address, Broadcast Address, Subnet Mask (e.g. $\text{255.255.255.0}$ for $/24$), and Total Usable Hosts using bitwise subnet masking. |
| **Network Interfaces** | Enumerates physical interfaces (Wi-Fi, Ethernet), loopback, and virtual tunnel interfaces via `java.net.NetworkInterface`. Inspects Hardware/MAC addresses and Maximum Transmission Unit ($\text{MTU}$). |
| **TCP/IP Protocol Suite** | Implements standard TCP stream socket communication with multi-threaded socket handling (`java.net.Socket`, `java.net.ServerSocket`). |
| **Client-Server Architecture** | Features an embedded multi-threaded Java Benchmark Server and Client communicating over custom TCP/UDP application protocols. |
| **Latency & Round Trip Time (RTT)** | Real-time probe tool measures socket connect latency and round-trip time in milliseconds ($\text{ms}$). |
| **Jitter & Packet Loss** | Calculates RFC 1889 Mean Absolute Deviation jitter: $\text{Jitter} = \frac{1}{N-1} \sum_{i=1}^{N-1} |\text{RTT}_{i+1} - \text{RTT}_i|$ and packet loss percentage. |
| **Packet Communication (UDP)** | Sends sequenced UDP Datagram packets (`java.net.DatagramPacket`, `DatagramSocket`) to evaluate connectionless packet delivery and loss. |

---

## ✨ 3. Key Features

### 1. Real-Time Bandwidth Monitoring (Dashboard)
- Instantaneous **Download Speed** and **Upload Speed** meters with dynamic unit auto-scaling ($\text{B/s}$, $\text{KB/s}$, $\text{MB/s}$, $\text{Mbps}$).
- Real-time KPI summary cards: Current Speed, Session Total Transferred, Peak Burst Speed, and Active Interface Link Status.
- Dropdown selector to monitor a specific interface (e.g., `en0` Wi-Fi) or aggregate all system interfaces.
- Configurable polling intervals ($\text{500 ms}$, $\text{1.0 s}$, $\text{2.0 s}$).

### 2. Live Bandwidth Visualizer (JFreeChart)
- High-performance, anti-aliased dual-series time graph:
  - **Download Traffic:** Cyan curve (`#38BDF8`)
  - **Upload Traffic:** Emerald curve (`#10B981`)
- Sliding time window (retains last 120 seconds of continuous traffic).
- Toggle button to dynamically switch graph vertical axis between $\text{KB/s}$ and $\text{Mbps}$.

### 3. Traffic & Protocol Analysis
- Visual data flow ratio bar displaying Inbound vs. Outbound traffic percentage.
- Packet counter tracking (total inbound vs outbound frames).
- Comprehensive transmission table showing Current, Peak, and Session Average speeds for both traffic directions.

### 4. Interface Explorer & Subnet Calculator
- Scans all system network interfaces displaying Status ($\text{UP/DOWN}$), MAC Address, Primary IPv4, Subnet Mask, MTU, and Loopback flag.
- Integrated **Subnet Math Engine**: displays CIDR prefix, Subnet Mask, Network Address, Broadcast Address, Host Range, Total Host Capacity, Address Class, and Private/Public scope.

### 5. Bandwidth Alerts & Anomaly Detection
- **Speed Limit Alert:** Triggers visual badge and optional audio beep when transfer rate exceeds a user-configured limit ($\text{MB/s}$).
- **Data Quota Limit:** Alerts the user when cumulative session consumption exceeds quota ($\text{MB}$).
- **Spike Anomaly Detection:** Rolling-average algorithm detects sudden traffic surges ($\ge 3.5\times$ rolling 10-second moving average).
- Incident history table logging timestamps, severity levels ($\text{INFO}$, $\text{WARNING}$, $\text{CRITICAL}$), and trigger values.

### 6. Computer Networks Experimental Lab
- **Latency & Jitter (Ping) Tool:** Probes remote hosts (e.g., Google DNS `8.8.8.8`, Cloudflare `1.1.1.1`, or local gateway) and computes Min RTT, Max RTT, Avg RTT, Jitter, and Packet Loss $\%$.
- **Client-Server Throughput Benchmark:**
  - One-click **Start Local Server** toggle (listens on port 9876).
  - Client benchmark connects, streams configurable payloads ($2\,\text{MB}$, $5\,\text{MB}$, $10\,\text{MB}$, $20\,\text{MB}$), calculates actual throughput in $\text{Mbps}$, and fires UDP datagrams to measure packet reliability.

### 7. Historical Reports & SQLite Persistence
- Automatically records bandwidth snapshots, monitoring sessions, and alert events into `network_monitor.db` via SQLite JDBC.
- Daily usage rollup table showing daily peak speed and record counts.
- **Export to CSV:** Generates tabular comma-separated values file for spreadsheet analysis.
- **Export to HTML:** Generates an executive performance report with dark UI styling, KPI cards, alerts, and snapshot tables.

---

## 🛠️ 4. Technology Stack

- **Core Language:** Java Standard Edition (Java SE 17+)
- **GUI Framework:** Java Swing (`JFrame`, `JTabbedPane`, `JTable`, `Nimbus L&F`)
- **Networking APIs:**
  - `java.net.NetworkInterface` (Interface discovery & MAC)
  - `java.net.InetAddress`, `java.net.InterfaceAddress` (IP and prefix length)
  - `java.net.Socket`, `java.net.ServerSocket` (TCP Client-Server communication)
  - `java.net.DatagramSocket`, `java.net.DatagramPacket` (UDP datagram communication)
- **Database:** SQLite 3 via `org.sqlite.JDBC`
- **Charting Engine:** JFreeChart 1.5.4
- **Concurrency:** `ScheduledExecutorService`, `Executors.newCachedThreadPool()`, Java Threads

---

## 📁 5. Project Architecture & Directory Structure

```text
cn project/
├── build.sh                      # One-click build script (compiles Java source)
├── run.sh                        # One-click launch script
├── package.sh                    # Packages project into standalone Fat JAR
├── sample_network_report.html    # Sample generated HTML performance report
├── sample_network_traffic.csv    # Sample generated CSV traffic data
├── network_monitor.db            # SQLite database file
├── lib/                          # Bundled JAR dependencies
│   ├── jfreechart-1.5.4.jar
│   ├── sqlite-jdbc-3.45.1.0.jar
│   ├── slf4j-api-2.0.12.jar
│   └── slf4j-simple-2.0.12.jar
├── dist/
│   └── NetPulse.jar              # Standalone runnable Fat JAR (15 MB)
└── src/
    └── com/
        └── netmonitor/
            ├── Main.java         # Application entry point & CLI diagnostics
            ├── model/
            │   ├── BandwidthSample.java      # Instantaneous metrics snapshot
            │   ├── NetworkInterfaceInfo.java # Interface metadata & addresses
            │   ├── AlertRecord.java          # Anomaly & threshold alert record
            │   ├── SessionSummary.java       # Session aggregate statistics
            │   └── PingResult.java           # Latency probe result model
            ├── service/
            │   ├── SystemTrafficReader.java  # OS network counter parser (macOS/Linux/Win)
            │   ├── NetworkInterfaceService.java # java.net interface & subnet calculations
            │   ├── TrafficMonitorService.java   # Periodic background polling engine
            │   ├── DatabaseService.java      # JDBC + SQLite storage & query engine
            │   ├── AlertService.java         # Rule evaluation & anomaly detection
            │   ├── PingService.java          # ICMP / Socket RTT & Jitter prober
            │   ├── BenchmarkServer.java      # Multi-threaded TCP/UDP test server
            │   ├── BenchmarkClient.java      # TCP/UDP throughput test client
            │   └── ReportService.java        # CSV & styled HTML report generator
            └── ui/
                ├── UITheme.java              # Modern Dark palette & styling
                ├── MetricCard.java           # Reusable KPI card component
                ├── BandwidthChartPanel.java  # Real-time JFreeChart component
                ├── DashboardPanel.java       # Main live bandwidth dashboard
                ├── TrafficAnalysisPanel.java # Protocol & traffic ratio view
                ├── InterfacePanel.java       # Interface list & subnet inspector
                ├── CNLabPanel.java           # Ping & Client-Server benchmark lab
                ├── AlertsPanel.java          # Threshold settings & alert log
                ├── HistoryReportsPanel.java  # SQLite history & report exporter
                └── MainFrame.java            # Main window & tabbed container
```

---

## 🚀 6. How to Build and Run

### Prerequisites
- Any modern Java SE Development Kit (**Java 17, 21, or 24**). Verify with:
  ```bash
  java -version
  javac -version
  ```

---

### Option A: One-Click Launch Script (Recommended)
Simply run the included shell script from the project root:
```bash
./run.sh
```
*If permissions need to be granted, run `chmod +x run.sh build.sh package.sh` first.*

---

### Option B: Run Standalone Executable JAR
A pre-packaged standalone Fat JAR (`dist/NetPulse.jar`) is ready to run:
```bash
java -jar dist/NetPulse.jar
```
*(On desktop environments, you can also double-click `NetPulse.jar` to open the GUI directly).*

---

### Option C: Rebuild and Package from Scratch
1. **Compile all sources:**
   ```bash
   ./build.sh
   ```
2. **Package into Standalone Fat JAR:**
   ```bash
   ./package.sh
   ```

---

### Option D: CLI Diagnostics & Headless Mode
To run automated system diagnostics or test interface detection from the command line:
```bash
./run.sh --test
```
To run continuous command-line network polling without opening a GUI window:
```bash
./run.sh --cli
```

---

## 🗄️ 7. Database Schema (SQLite + JDBC)

All monitoring history is saved locally in `network_monitor.db`. The schema includes three structured tables:

### 1. `bandwidth_samples`
```sql
CREATE TABLE bandwidth_samples (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,          -- Unix epoch time (ms)
    interface_name TEXT NOT NULL,        -- e.g. 'en0'
    download_bps REAL NOT NULL,          -- Bytes/sec download
    upload_bps REAL NOT NULL,            -- Bytes/sec upload
    total_in INTEGER NOT NULL,           -- Cumulative received bytes
    total_out INTEGER NOT NULL,          -- Cumulative sent bytes
    packets_in INTEGER NOT NULL,         -- Inbound packet count
    packets_out INTEGER NOT NULL         -- Outbound packet count
);
```

### 2. `monitoring_sessions`
```sql
CREATE TABLE monitoring_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    start_time INTEGER NOT NULL,
    end_time INTEGER NOT NULL,
    interface_name TEXT NOT NULL,
    total_in INTEGER NOT NULL,
    total_out INTEGER NOT NULL,
    peak_down_bps REAL NOT NULL,
    peak_up_bps REAL NOT NULL,
    avg_down_bps REAL NOT NULL,
    avg_up_bps REAL NOT NULL
);
```

### 3. `alerts`
```sql
CREATE TABLE alerts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    level TEXT NOT NULL,                 -- 'INFO', 'WARNING', 'CRITICAL'
    rule_type TEXT NOT NULL,             -- 'SPEED_LIMIT', 'DATA_QUOTA', 'TRAFFIC_SPIKE'
    message TEXT NOT NULL,
    value REAL NOT NULL,
    threshold REAL NOT NULL
);
```

---

## 📊 8. Reporting Engine (HTML & CSV Exports)

NetPulse Pro includes built-in reporting:
1. **CSV Export:** Click **Export to CSV** in the *History & Reports* tab to save all captured data points into standard CSV format for Excel, Python Pandas, or MATLAB.
2. **HTML Performance Report:** Click **Export HTML Report** to generate a styled dashboard report (`sample_network_report.html`) containing:
   - Session duration and total volume metrics
   - Inbound vs. Outbound breakdown
   - Peak and average throughput
   - Full anomaly and alert incident log
   - Tabular history of network snapshots

---

## 🎓 9. Viva & Academic Presentation Guide

When presenting this project for an evaluation, lab exam, or project viva, refer to these key points:

### Q1: How does Java read physical interface byte counters without external C libraries?
> **Answer:** In modern operating systems, hardware interface statistics are maintained by the kernel's network stack. NetPulse Pro's `SystemTrafficReader` interrogates standard OS subsystem interfaces:
> - On **macOS / BSD**: Queries `netstat -ibn` link-layer rows (`<Link#X>`) to read cumulative byte counters (`Ibytes`, `Obytes`) and packet counters (`Ipkts`, `Opkts`).
> - On **Linux**: Parses kernel counters directly from `/proc/net/dev`.
> - On **Windows**: Queries `netstat -e`.
> The service then calculates:
> $$\text{Speed (B/s)} = \frac{\text{Current Bytes} - \text{Previous Bytes}}{\Delta \text{Time in seconds}}$$

### Q2: What is the difference between Bandwidth and Throughput?
> **Answer:** 
> - **Bandwidth** is the theoretical maximum transmission capacity of a network link (e.g., a $1\,\text{Gbps}$ Ethernet interface or $150\,\text{Mbps}$ Wi-Fi channel).
> - **Throughput** is the actual rate of successful data delivery over the channel after factoring in protocol overhead (TCP headers, IP headers, Ethernet frames), network contention, packet loss, and latency.
> In our project, the **CN Lab & Benchmarks** tab lets the user run a live TCP socket stream to measure actual throughput against theoretical capacity.

### Q3: How is Subnetting calculated programmatically?
> **Answer:** When an interface has an IPv4 address (e.g. `192.168.1.2/24`), the prefix length ($24$) defines the subnet mask.
> - Subnet Mask = `0xFFFFFFFF << (32 - 24)` $\rightarrow$ `255.255.255.0`
> - Network Address = $\text{IP} \;\mathbf{AND}\; \text{Mask}$ $\rightarrow$ `192.168.1.0`
> - Broadcast Address = $\text{Network Address} \;\mathbf{OR}\; (\mathbf{NOT}\;\text{Mask})$ $\rightarrow$ `192.168.1.255`
> - Usable Host Range = `192.168.1.1` to `192.168.1.254` ($2^{32-24} - 2 = 254$ hosts).
> Our `NetworkInterfaceService` performs this bitwise computation dynamically.

### Q4: How is Jitter measured?
> **Answer:** As defined in **RFC 1889**, Jitter is the statistical variation in packet transit delay. If consecutive ping probes return round trip times of $R_1, R_2, \dots, R_N$, jitter is calculated as the mean absolute difference between successive delay samples:
> $$\text{Jitter} = \frac{1}{N-1} \sum_{i=1}^{N-1} |\text{RTT}_{i+1} - \text{RTT}_i|$$
> A low jitter value indicates a stable, low-latency connection suitable for real-time applications like VoIP and video streaming.

---

## 📄 License
This project is open-source and developed for academic and practical networking demonstrations.
