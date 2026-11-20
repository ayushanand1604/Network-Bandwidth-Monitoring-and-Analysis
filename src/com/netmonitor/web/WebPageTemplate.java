package com.netmonitor.web;

/**
 * Modern Responsive Web Application Template (HTML5, CSS3, Vanilla JS).
 * Works on Mobile, Tablet, and Desktop browsers.
 */
public class WebPageTemplate {

    public static String getHtml(int port) {
        return "<!DOCTYPE html>\n" +
"<html lang='en'>\n" +
"<head>\n" +
"  <meta charset='UTF-8'>\n" +
"  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
"  <title>NetPulse Pro • Web Bandwidth & Network Monitor</title>\n" +
"  <style>\n" +
"    :root {\n" +
"      --bg-dark: #0f172a;\n" +
"      --bg-card: #1e293b;\n" +
"      --bg-card-light: #334155;\n" +
"      --text-main: #f8fafc;\n" +
"      --text-muted: #94a3b8;\n" +
"      --cyan: #38bdf8;\n" +
"      --emerald: #10b981;\n" +
"      --amber: #f59e0b;\n" +
"      --rose: #f43f5e;\n" +
"      --purple: #a855f7;\n" +
"      --border: #334155;\n" +
"    }\n" +
"    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, sans-serif; }\n" +
"    body { background-color: var(--bg-dark); color: var(--text-main); min-height: 100vh; display: flex; flex-direction: column; }\n" +
"    \n" +
"    /* Header */\n" +
"    header { background: #0b1120; border-bottom: 1px solid var(--border); padding: 14px 24px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }\n" +
"    .logo { display: flex; align-items: center; gap: 10px; font-weight: bold; font-size: 18px; color: var(--text-main); }\n" +
"    .logo-badge { font-size: 11px; padding: 3px 8px; border-radius: 999px; background: rgba(16, 185, 129, 0.15); color: var(--emerald); border: 1px solid var(--emerald); }\n" +
"    .host-info { font-size: 12px; color: var(--text-muted); }\n" +
"    \n" +
"    /* Nav Tabs */\n" +
"    nav { display: flex; background: #131d33; border-bottom: 1px solid var(--border); overflow-x: auto; padding: 0 16px; }\n" +
"    .tab-btn { background: none; border: none; color: var(--text-muted); padding: 12px 18px; font-size: 13px; font-weight: 600; cursor: pointer; border-bottom: 2px solid transparent; white-space: nowrap; transition: all 0.2s; }\n" +
"    .tab-btn:hover { color: var(--text-main); }\n" +
"    .tab-btn.active { color: var(--cyan); border-bottom-color: var(--cyan); }\n" +
"    \n" +
"    /* Main Container */\n" +
"    main { max-width: 1200px; width: 100%; margin: 0 auto; padding: 20px 16px; flex: 1; }\n" +
"    .tab-content { display: none; }\n" +
"    .tab-content.active { display: block; }\n" +
"    \n" +
"    /* KPI Grid */\n" +
"    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; margin-bottom: 20px; }\n" +
"    .card { background: var(--bg-card); border-radius: 10px; padding: 16px; border: 1px solid var(--border); position: relative; overflow: hidden; }\n" +
"    .card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; }\n" +
"    .card.cyan::before { background: var(--cyan); }\n" +
"    .card.emerald::before { background: var(--emerald); }\n" +
"    .card.amber::before { background: var(--amber); }\n" +
"    .card.purple::before { background: var(--purple); }\n" +
"    .card-title { font-size: 11px; text-transform: uppercase; color: var(--text-muted); font-weight: 700; letter-spacing: 0.5px; }\n" +
"    .card-val { font-size: 24px; font-weight: 700; margin: 6px 0 2px 0; }\n" +
"    .card-sub { font-size: 12px; color: var(--text-muted); }\n" +
"    \n" +
"    /* Chart Container */\n" +
"    .chart-box { background: var(--bg-card); border-radius: 10px; border: 1px solid var(--border); padding: 16px; margin-bottom: 20px; }\n" +
"    .chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }\n" +
"    canvas { width: 100%; height: 260px; display: block; background: #131b2e; border-radius: 6px; }\n" +
"    \n" +
"    /* Tables */\n" +
"    table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; }\n" +
"    th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid var(--border); }\n" +
"    th { background: #182235; color: var(--text-muted); font-size: 11px; text-transform: uppercase; }\n" +
"    tr:hover { background: rgba(51, 65, 85, 0.4); }\n" +
"    \n" +
"    /* Buttons */\n" +
"    .btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 8px 16px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; transition: 0.2s; text-decoration: none; }\n" +
"    .btn-cyan { background: var(--cyan); color: #0b1120; }\n" +
"    .btn-cyan:hover { background: #7dd3fc; }\n" +
"    .btn-emerald { background: var(--emerald); color: #0b1120; }\n" +
"    .btn-emerald:hover { background: #34d399; }\n" +
"    .btn-purple { background: var(--purple); color: white; }\n" +
"    .btn-purple:hover { background: #c084fc; }\n" +
"    .btn-outline { background: transparent; border: 1px solid var(--border); color: var(--text-main); }\n" +
"    .btn-outline:hover { background: var(--bg-card-light); }\n" +
"    \n" +
"    /* Speedtest Widget */\n" +
"    .speedtest-wrap { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; text-align: center; }\n" +
"    .gauge-circle { width: 220px; height: 220px; border-radius: 50%; border: 8px solid #334155; display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 24px auto; position: relative; transition: border-color 0.4s; }\n" +
"    .gauge-circle.testing { border-color: var(--cyan); box-shadow: 0 0 30px rgba(56, 189, 248, 0.2); }\n" +
"    .gauge-speed { font-size: 44px; font-weight: 800; color: var(--cyan); }\n" +
"    .gauge-unit { font-size: 14px; color: var(--text-muted); font-weight: 600; }\n" +
"    .speedtest-metrics { display: flex; gap: 30px; margin-top: 20px; justify-content: center; flex-wrap: wrap; }\n" +
"    .sm-box { text-align: center; min-width: 100px; }\n" +
"    .sm-title { font-size: 12px; color: var(--text-muted); text-transform: uppercase; }\n" +
"    .sm-val { font-size: 20px; font-weight: 700; margin-top: 4px; }\n" +
"    \n" +
"    /* Forms & Inputs */\n" +
"    input, select { background: #131b2e; border: 1px solid var(--border); color: var(--text-main); padding: 8px 12px; border-radius: 6px; font-size: 13px; outline: none; }\n" +
"    input:focus, select:focus { border-color: var(--cyan); }\n" +
"    \n" +
"    /* Footer */\n" +
"    footer { text-align: center; padding: 16px; font-size: 12px; color: #64748b; border-top: 1px solid var(--border); background: #0b1120; }\n" +
"  </style>\n" +
"</head>\n" +
"<body>\n" +
"  <header>\n" +
"    <div class='logo'>\n" +
"      <span>⚡ NetPulse Web</span>\n" +
"      <span class='logo-badge'>ONLINE</span>\n" +
"    </div>\n" +
"    <div class='host-info' id='hostLabel'>Host: Scanning interfaces...</div>\n" +
"  </header>\n" +
"\n" +
"  <nav>\n" +
"    <button class='tab-btn active' onclick=\"switchTab('dashboard')\">Live Dashboard</button>\n" +
"    <button class='tab-btn' onclick=\"switchTab('speedtest')\">🚀 Browser Speedtest</button>\n" +
"    <button class='tab-btn' onclick=\"switchTab('interfaces')\">Interfaces & Subnets</button>\n" +
"    <button class='tab-btn' onclick=\"switchTab('ping')\">Ping & Latency</button>\n" +
"    <button class='tab-btn' onclick=\"switchTab('reports')\">History & Reports</button>\n" +
"  </nav>\n" +
"\n" +
"  <main>\n" +
"    <!-- Tab 1: Live Dashboard -->\n" +
"    <section id='tab-dashboard' class='tab-content active'>\n" +
"      <div class='kpi-grid'>\n" +
"        <div class='card cyan'>\n" +
"          <div class='card-title'>Download Speed</div>\n" +
"          <div class='card-val' id='cardDl'>0.00 KB/s</div>\n" +
"          <div class='card-sub' id='cardDlMbps'>0.00 Mbps</div>\n" +
"        </div>\n" +
"        <div class='card emerald'>\n" +
"          <div class='card-title'>Upload Speed</div>\n" +
"          <div class='card-val' id='cardUl'>0.00 KB/s</div>\n" +
"          <div class='card-sub' id='cardUlMbps'>0.00 Mbps</div>\n" +
"        </div>\n" +
"        <div class='card cyan'>\n" +
"          <div class='card-title'>Total Received</div>\n" +
"          <div class='card-val' id='cardTotIn'>0.00 MB</div>\n" +
"          <div class='card-sub' id='cardPktsIn'>Packets: 0</div>\n" +
"        </div>\n" +
"        <div class='card emerald'>\n" +
"          <div class='card-title'>Total Sent</div>\n" +
"          <div class='card-val' id='cardTotOut'>0.00 MB</div>\n" +
"          <div class='card-sub' id='cardPktsOut'>Packets: 0</div>\n" +
"        </div>\n" +
"        <div class='card purple'>\n" +
"          <div class='card-title'>Peak Download</div>\n" +
"          <div class='card-val' id='cardPeakDl'>0.00 KB/s</div>\n" +
"          <div class='card-sub'>Session Peak</div>\n" +
"        </div>\n" +
"        <div class='card amber'>\n" +
"          <div class='card-title'>Active Interface</div>\n" +
"          <div class='card-val' id='cardIface'>en0</div>\n" +
"          <div class='card-sub' id='cardLinkStatus'>Status: Connected</div>\n" +
"        </div>\n" +
"      </div>\n" +
"\n" +
"      <div class='chart-box'>\n" +
"        <div class='chart-header'>\n" +
"          <span style='font-weight: 700; font-size: 13px; color: var(--text-muted);'>LIVE BANDWIDTH STREAM (SLIDING WINDOW)</span>\n" +
"          <span style='font-size: 12px; color: var(--cyan);'>● Download &nbsp; <span style='color: var(--emerald);'>● Upload</span></span>\n" +
"        </div>\n" +
"        <canvas id='bandwidthCanvas'></canvas>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- Tab 2: Browser Speedtest -->\n" +
"    <section id='tab-speedtest' class='tab-content'>\n" +
"      <div class='card' style='text-align: center; padding: 30px;'>\n" +
"        <h2 style='color: var(--cyan); margin-bottom: 8px;'>Interactive Web Speedtest</h2>\n" +
"        <p style='color: var(--text-muted); font-size: 14px;'>Measures throughput directly between your browser and the NetPulse Java server.</p>\n" +
"        \n" +
"        <div class='speedtest-wrap'>\n" +
"          <div class='gauge-circle' id='gauge'>\n" +
"            <div class='gauge-speed' id='speedDisplay'>0.0</div>\n" +
"            <div class='gauge-unit' id='speedUnit'>Mbps</div>\n" +
"          </div>\n" +
"          \n" +
"          <button class='btn btn-cyan' id='startSpeedtestBtn' onclick='runWebSpeedtest()' style='padding: 12px 30px; font-size: 16px;'>Start Speed Test</button>\n" +
"          \n" +
"          <div class='speedtest-metrics'>\n" +
"            <div class='sm-box'><div class='sm-title'>Ping Latency</div><div class='sm-val' id='stPing' style='color: var(--amber);'>-- ms</div></div>\n" +
"            <div class='sm-box'><div class='sm-title'>Jitter</div><div class='sm-val' id='stJitter' style='color: var(--amber);'>-- ms</div></div>\n" +
"            <div class='sm-box'><div class='sm-title'>Download Speed</div><div class='sm-val' id='stDl' style='color: var(--cyan);'>-- Mbps</div></div>\n" +
"            <div class='sm-box'><div class='sm-title'>Upload Speed</div><div class='sm-val' id='stUl' style='color: var(--emerald);'>-- Mbps</div></div>\n" +
"          </div>\n" +
"        </div>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- Tab 3: Interfaces & Subnets -->\n" +
"    <section id='tab-interfaces' class='tab-content'>\n" +
"      <div class='card' style='margin-bottom: 20px;'>\n" +
"        <h3 style='margin-bottom: 12px; color: var(--cyan);'>Host Network Interfaces</h3>\n" +
"        <div style='overflow-x: auto;'>\n" +
"          <table>\n" +
"            <thead>\n" +
"              <tr><th>Interface</th><th>Display Name</th><th>Status</th><th>MAC Address</th><th>IPv4</th><th>Subnet Mask</th><th>MTU</th></tr>\n" +
"            </thead>\n" +
"            <tbody id='interfacesBody'>\n" +
"              <tr><td colspan='7'>Loading interfaces...</td></tr>\n" +
"            </tbody>\n" +
"          </table>\n" +
"        </div>\n" +
"      </div>\n" +
"\n" +
"      <!-- Subnet Calculator -->\n" +
"      <div class='card'>\n" +
"        <h3 style='margin-bottom: 12px; color: var(--emerald);'>Interactive Subnet & CIDR Calculator</h3>\n" +
"        <div style='display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-bottom: 16px;'>\n" +
"          <label>IP Address: <input type='text' id='subCalcIp' value='192.168.1.100' style='width: 140px;'></label>\n" +
"          <label>Prefix Length: \n" +
"            <select id='subCalcPrefix'>\n" +
"              <option value='8'>/8 (255.0.0.0)</option>\n" +
"              <option value='16'>/16 (255.255.0.0)</option>\n" +
"              <option value='24' selected>/24 (255.255.255.0)</option>\n" +
"              <option value='25'>/25 (255.255.255.128)</option>\n" +
"              <option value='26'>/26 (255.255.255.192)</option>\n" +
"              <option value='27'>/27 (255.255.255.224)</option>\n" +
"              <option value='28'>/28 (255.255.255.240)</option>\n" +
"              <option value='29'>/29 (255.255.255.248)</option>\n" +
"              <option value='30'>/30 (255.255.255.252)</option>\n" +
"            </select>\n" +
"          </label>\n" +
"          <button class='btn btn-emerald' onclick='calculateSubnet()'>Calculate Subnet</button>\n" +
"        </div>\n" +
"        <div id='subnetResult' style='font-family: monospace; font-size: 13px; line-height: 1.8; color: var(--text-muted); background: #131b2e; padding: 14px; border-radius: 6px;'>\n" +
"          Click 'Calculate Subnet' to inspect bitwise network boundaries.\n" +
"        </div>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- Tab 4: Ping & Latency -->\n" +
"    <section id='tab-ping' class='tab-content'>\n" +
"      <div class='card'>\n" +
"        <h3 style='margin-bottom: 12px; color: var(--amber);'>Server-Side Latency & Jitter Probe (Ping)</h3>\n" +
"        <div style='display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-bottom: 16px;'>\n" +
"          <label>Target Host/IP: <input type='text' id='pingHost' value='8.8.8.8' style='width: 160px;'></label>\n" +
"          <label>Count: <input type='number' id='pingCount' value='4' min='1' max='20' style='width: 60px;'></label>\n" +
"          <button class='btn btn-cyan' id='pingBtn' onclick='runServerPing()'>Send Ping Probes</button>\n" +
"        </div>\n" +
"        <div id='pingOutput' style='font-family: monospace; font-size: 13px; line-height: 1.6; color: var(--cyan); background: #131b2e; padding: 16px; border-radius: 6px; min-height: 140px; white-space: pre-wrap;'>Ready. Enter host to measure RTT.</div>\n" +
"      </div>\n" +
"    </section>\n" +
"\n" +
"    <!-- Tab 5: Reports -->\n" +
"    <section id='tab-reports' class='tab-content'>\n" +
"      <div class='card' style='margin-bottom: 20px;'>\n" +
"        <div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;'>\n" +
"          <h3 style='color: var(--purple);'>Export Network Performance Reports</h3>\n" +
"          <div style='display: flex; gap: 10px;'>\n" +
"            <a href='/api/report/csv' class='btn btn-cyan' download>📥 Download CSV</a>\n" +
"            <a href='/api/report/html' class='btn btn-emerald' download>📄 Download HTML Report</a>\n" +
"          </div>\n" +
"        </div>\n" +
"        <p style='color: var(--text-muted); font-size: 13px;'>Exports the full historical database collected by SQLite JDBC.</p>\n" +
"      </div>\n" +
"\n" +
"      <div class='card'>\n" +
"        <h3 style='margin-bottom: 12px; color: var(--text-main);'>Recent Recorded Snapshots (network_monitor.db)</h3>\n" +
"        <div style='overflow-x: auto;'>\n" +
"          <table>\n" +
"            <thead>\n" +
"              <tr><th>Timestamp</th><th>Interface</th><th>Download Speed</th><th>Upload Speed</th><th>Total In</th><th>Total Out</th></tr>\n" +
"            </thead>\n" +
"            <tbody id='historyBody'>\n" +
"              <tr><td colspan='6'>Loading records...</td></tr>\n" +
"            </tbody>\n" +
"          </table>\n" +
"        </div>\n" +
"      </div>\n" +
"    </section>\n" +
"  </main>\n" +
"\n" +
"  <footer>\n" +
"    NetPulse Pro Web System &bull; Java SE 24 Computer Networks Project\n" +
"  </footer>\n" +
"\n" +
"  <script>\n" +
"    // Tab Switching\n" +
"    function switchTab(name) {\n" +
"      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));\n" +
"      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));\n" +
"      event.target.classList.add('active');\n" +
"      document.getElementById('tab-' + name).classList.add('active');\n" +
"      if (name === 'interfaces') loadInterfaces();\n" +
"      if (name === 'reports') loadHistory();\n" +
"    }\n" +
"\n" +
"    // Format helpers\n" +
"    function formatSpeed(bytes) {\n" +
"      if (bytes < 1024) return bytes.toFixed(2) + ' B/s';\n" +
"      if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB/s';\n" +
"      if (bytes < 1073741824) return (bytes / 1048576).toFixed(2) + ' MB/s';\n" +
"      return (bytes / 1073741824).toFixed(2) + ' GB/s';\n" +
"    }\n" +
"    function formatBytes(bytes) {\n" +
"      if (bytes < 1024) return bytes + ' B';\n" +
"      if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB';\n" +
"      if (bytes < 1073741824) return (bytes / 1048576).toFixed(2) + ' MB';\n" +
"      return (bytes / 1073741824).toFixed(2) + ' GB';\n" +
"    }\n" +
"\n" +
"    // Real-Time Canvas Line Chart\n" +
"    const canvas = document.getElementById('bandwidthCanvas');\n" +
"    const ctx = canvas.getContext('2d');\n" +
"    const maxPoints = 50;\n" +
"    const dlHistory = new Array(maxPoints).fill(0);\n" +
"    const ulHistory = new Array(maxPoints).fill(0);\n" +
"\n" +
"    function drawChart() {\n" +
"      canvas.width = canvas.parentElement.clientWidth - 32;\n" +
"      canvas.height = 240;\n" +
"      const w = canvas.width, h = canvas.height;\n" +
"      ctx.clearRect(0, 0, w, h);\n" +
"\n" +
"      // Gridlines\n" +
"      ctx.strokeStyle = '#243049';\n" +
"      ctx.lineWidth = 1;\n" +
"      for (let y = 40; y < h; y += 50) {\n" +
"        ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(w, y); ctx.stroke();\n" +
"      }\n" +
"\n" +
"      const maxVal = Math.max(100, ...dlHistory, ...ulHistory) * 1.2;\n" +
"      \n" +
"      function plotSeries(arr, color) {\n" +
"        ctx.strokeStyle = color;\n" +
"        ctx.lineWidth = 2.5;\n" +
"        ctx.beginPath();\n" +
"        arr.forEach((v, i) => {\n" +
"          const x = (i / (maxPoints - 1)) * w;\n" +
"          const y = h - (v / maxVal) * (h - 20) - 10;\n" +
"          if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);\n" +
"        });\n" +
"        ctx.stroke();\n" +
"      }\n" +
"\n" +
"      plotSeries(dlHistory, '#38bdf8');\n" +
"      plotSeries(ulHistory, '#10b981');\n" +
"    }\n" +
"\n" +
"    // Live Polling\n" +
"    async function pollStats() {\n" +
"      try {\n" +
"        const res = await fetch('/api/stats');\n" +
"        const data = await res.json();\n" +
"        document.getElementById('cardDl').innerText = formatSpeed(data.downloadBps);\n" +
"        document.getElementById('cardDlMbps').innerText = data.downloadMbps.toFixed(2) + ' Mbps';\n" +
"        document.getElementById('cardUl').innerText = formatSpeed(data.uploadBps);\n" +
"        document.getElementById('cardUlMbps').innerText = data.uploadMbps.toFixed(2) + ' Mbps';\n" +
"        document.getElementById('cardTotIn').innerText = formatBytes(data.totalIn);\n" +
"        document.getElementById('cardTotOut').innerText = formatBytes(data.totalOut);\n" +
"        document.getElementById('cardPktsIn').innerText = 'Packets: ' + data.packetsIn;\n" +
"        document.getElementById('cardPktsOut').innerText = 'Packets: ' + data.packetsOut;\n" +
"        document.getElementById('cardPeakDl').innerText = formatSpeed(data.peakDownBps);\n" +
"        document.getElementById('cardIface').innerText = data.interface;\n" +
"\n" +
"        dlHistory.push(data.downloadBps / 1024.0); dlHistory.shift();\n" +
"        ulHistory.push(data.uploadBps / 1024.0); ulHistory.shift();\n" +
"        drawChart();\n" +
"      } catch (e) {}\n" +
"    }\n" +
"    setInterval(pollStats, 1000);\n" +
"    pollStats();\n" +
"\n" +
"    // Interfaces Loader\n" +
"    async function loadInterfaces() {\n" +
"      try {\n" +
"        const res = await fetch('/api/interfaces');\n" +
"        const data = await res.json();\n" +
"        let html = '';\n" +
"        data.forEach(item => {\n" +
"          html += `<tr>\n" +
"            <td><strong>${item.name}</strong></td>\n" +
"            <td>${item.displayName}</td>\n" +
"            <td><span style='color: ${item.isUp ? 'var(--emerald)' : 'var(--rose)'}'>● ${item.isUp ? 'UP' : 'DOWN'}</span></td>\n" +
"            <td><code>${item.mac}</code></td>\n" +
"            <td>${item.ipv4}</td>\n" +
"            <td>${item.subnet}</td>\n" +
"            <td>${item.mtu > 0 ? item.mtu + ' B' : 'N/A'}</td>\n" +
"          </tr>`;\n" +
"        });\n" +
"        document.getElementById('interfacesBody').innerHTML = html;\n" +
"        if (data.length > 0) {\n" +
"          const p = data.find(i => i.isUp && !i.isLoopback) || data[0];\n" +
"          document.getElementById('hostLabel').innerText = `Primary Host: ${p.name} (${p.ipv4}) • Port " + port + "`;\n" +
"        }\n" +
"      } catch (e) {}\n" +
"    }\n" +
"    loadInterfaces();\n" +
"\n" +
"    // Subnet Calculation\n" +
"    function calculateSubnet() {\n" +
"      const ip = document.getElementById('subCalcIp').value.trim();\n" +
"      const prefix = parseInt(document.getElementById('subCalcPrefix').value);\n" +
"      const parts = ip.split('.').map(Number);\n" +
"      if (parts.length !== 4 || parts.some(p => isNaN(p) || p < 0 || p > 255)) {\n" +
"        document.getElementById('subnetResult').innerText = 'Error: Invalid IPv4 address';\n" +
"        return;\n" +
"      }\n" +
"      const ipInt = ((parts[0] << 24) | (parts[1] << 16) | (parts[2] << 8) | parts[3]) >>> 0;\n" +
"      const mask = prefix === 0 ? 0 : ((0xFFFFFFFF << (32 - prefix)) >>> 0);\n" +
"      const netInt = (ipInt & mask) >>> 0;\n" +
"      const bcastInt = (netInt | (~mask >>> 0)) >>> 0;\n" +
"      \n" +
"      const intToIp = (val) => `${(val >>> 24) & 255}.${(val >>> 16) & 255}.${(val >>> 8) & 255}.${val & 255}`;\n" +
"      const total = Math.pow(2, 32 - prefix);\n" +
"      const usable = total > 2 ? total - 2 : total;\n" +
"\n" +
"      let out = `CIDR Notation:        ${ip}/${prefix}\\n`;\n" +
"      out += `Subnet Mask:          ${intToIp(mask)}\\n`;\n" +
"      out += `Network Address:      ${intToIp(netInt)}\\n`;\n" +
"      out += `Broadcast Address:    ${intToIp(bcastInt)}\\n`;\n" +
"      out += `Usable Host Range:    ${intToIp(netInt + 1)} - ${intToIp(bcastInt - 1)}\\n`;\n" +
"      out += `Total Host Capacity:  ${total.toLocaleString()}\\n`;\n" +
"      out += `Usable Hosts:         ${usable.toLocaleString()}\\n`;\n" +
"      document.getElementById('subnetResult').innerText = out;\n" +
"    }\n" +
"\n" +
"    // Server Ping\n" +
"    async function runServerPing() {\n" +
"      const host = document.getElementById('pingHost').value.trim();\n" +
"      const count = document.getElementById('pingCount').value;\n" +
"      const btn = document.getElementById('pingBtn');\n" +
"      const out = document.getElementById('pingOutput');\n" +
"      btn.disabled = true;\n" +
"      out.innerText = `Pinging ${host} with ${count} packets from Java backend server...\\n`;\n" +
"      try {\n" +
"        const res = await fetch(`/api/ping?host=${encodeURIComponent(host)}&count=${count}`);\n" +
"        const data = await res.json();\n" +
"        if (data.error) {\n" +
"          out.innerText += `\\n[ERROR] ${data.error}`;\n" +
"        } else {\n" +
"          out.innerText += `\\n--- Ping Statistics for ${data.host} ---\\n`;\n" +
"          out.innerText += `Packets: Sent = ${data.transmitted}, Received = ${data.received}, Lost = ${data.lost} (${data.lossRatePercent}% loss)\\n`;\n" +
"          out.innerText += `Approximate Round Trip Times: Min = ${data.minRtt}ms, Max = ${data.maxRtt}ms, Avg = ${data.avgRtt}ms\\n`;\n" +
"          out.innerText += `Jitter (Mean Deviation): ${data.jitter.toFixed(2)} ms\\n`;\n" +
"        }\n" +
"      } catch (e) {\n" +
"        out.innerText += `\\n[ERROR] Network request failed: ${e.message}`;\n" +
"      } finally {\n" +
"        btn.disabled = false;\n" +
"      }\n" +
"    }\n" +
"\n" +
"    // History Loader\n" +
"    async function loadHistory() {\n" +
"      try {\n" +
"        const res = await fetch('/api/history');\n" +
"        const data = await res.json();\n" +
"        let html = '';\n" +
"        (data.samples || []).forEach(s => {\n" +
"          html += `<tr>\n" +
"            <td>${s.time}</td>\n" +
"            <td>${s.iface}</td>\n" +
"            <td style='color: var(--cyan);'>${formatSpeed(s.dlBps)}</td>\n" +
"            <td style='color: var(--emerald);'>${formatSpeed(s.ulBps)}</td>\n" +
"            <td>${formatBytes(s.inBytes)}</td>\n" +
"            <td>${formatBytes(s.outBytes)}</td>\n" +
"          </tr>`;\n" +
"        });\n" +
"        document.getElementById('historyBody').innerHTML = html || '<tr><td colspan=6>No history</td></tr>';\n" +
"      } catch (e) {}\n" +
"    }\n" +
"\n" +
"    // Interactive Browser Speedtest\n" +
"    async function runWebSpeedtest() {\n" +
"      const btn = document.getElementById('startSpeedtestBtn');\n" +
"      const gauge = document.getElementById('gauge');\n" +
"      const display = document.getElementById('speedDisplay');\n" +
"      const unit = document.getElementById('speedUnit');\n" +
"      btn.disabled = true;\n" +
"      gauge.classList.add('testing');\n" +
"\n" +
"      // 1. Latency & Jitter\n" +
"      display.innerText = 'Ping'; unit.innerText = 'Measuring';\n" +
"      const pings = [];\n" +
"      for (let i = 0; i < 5; i++) {\n" +
"        const t0 = performance.now();\n" +
"        await fetch('/api/speedtest/ping?t=' + Date.now());\n" +
"        pings.push(performance.now() - t0);\n" +
"      }\n" +
"      const avgPing = pings.reduce((a,b)=>a+b,0) / pings.length;\n" +
"      let jitterSum = 0;\n" +
"      for (let j = 1; j < pings.length; j++) jitterSum += Math.abs(pings[j] - pings[j-1]);\n" +
"      const jitter = jitterSum / (pings.length - 1);\n" +
"      document.getElementById('stPing').innerText = avgPing.toFixed(1) + ' ms';\n" +
"      document.getElementById('stJitter').innerText = jitter.toFixed(1) + ' ms';\n" +
"\n" +
"      // 2. Download Speed\n" +
"      display.innerText = '0.0'; unit.innerText = 'Mbps (DL)';\n" +
"      const dlSize = 8 * 1024 * 1024; // 8 MB payload\n" +
"      const tStartDl = performance.now();\n" +
"      const dlRes = await fetch('/api/speedtest/download?size=' + dlSize + '&t=' + Date.now());\n" +
"      const reader = dlRes.body.getReader();\n" +
"      let dlBytes = 0;\n" +
"      while (true) {\n" +
"        const { done, value } = await reader.read();\n" +
"        if (done) break;\n" +
"        dlBytes += value.length;\n" +
"        const elSec = (performance.now() - tStartDl) / 1000;\n" +
"        const curMbps = (dlBytes * 8) / (elSec * 1000000);\n" +
"        display.innerText = curMbps.toFixed(1);\n" +
"      }\n" +
"      const totalDlSec = (performance.now() - tStartDl) / 1000;\n" +
"      const finalDlMbps = (dlBytes * 8) / (totalDlSec * 1000000);\n" +
"      document.getElementById('stDl').innerText = finalDlMbps.toFixed(2) + ' Mbps';\n" +
"\n" +
"      // 3. Upload Speed\n" +
"      display.innerText = '0.0'; unit.innerText = 'Mbps (UL)';\n" +
"      const ulPayload = new Uint8Array(4 * 1024 * 1024); // 4 MB payload\n" +
"      const tStartUl = performance.now();\n" +
"      const ulRes = await fetch('/api/speedtest/upload', {\n" +
"        method: 'POST',\n" +
"        body: ulPayload\n" +
"      });\n" +
"      const ulData = await ulRes.json();\n" +
"      const finalUlMbps = ulData.throughputMbps;\n" +
"      document.getElementById('stUl').innerText = finalUlMbps.toFixed(2) + ' Mbps';\n" +
"      display.innerText = finalDlMbps.toFixed(1);\n" +
"      unit.innerText = 'Mbps (Done)';\n" +
"\n" +
"      gauge.classList.remove('testing');\n" +
"      btn.disabled = false;\n" +
"      btn.innerText = 'Test Again';\n" +
"    }\n" +
"  </script>\n" +
"</body>\n" +
"</html>";
    }
}
