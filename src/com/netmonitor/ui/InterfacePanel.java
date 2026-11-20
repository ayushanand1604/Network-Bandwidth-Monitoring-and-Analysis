package com.netmonitor.ui;

import com.netmonitor.model.NetworkInterfaceInfo;
import com.netmonitor.service.NetworkInterfaceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Network Interface Explorer & Inspector.
 * Demonstrates Computer Networks concepts:
 * - Network Interfaces (Ethernet, Wi-Fi, Loopback, Virtual)
 * - MAC Hardware Addressing
 * - IP Addressing & CIDR Subnetting
 * - MTU (Maximum Transmission Unit)
 */
public class InterfacePanel extends JPanel {

    private final NetworkInterfaceService ifService;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea detailsArea;
    private List<NetworkInterfaceInfo> currentInterfaces;

    public InterfacePanel(NetworkInterfaceService ifService) {
        this.ifService = ifService;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(UITheme.createPaddedBorder(16, 16, 16, 16));

        initHeader();
        initContent();
        loadInterfaces();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Network Interface Explorer & Subnet Inspector");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JButton refreshBtn = UITheme.createStyledButton("Rescan Interfaces", UITheme.ACCENT_CYAN);
        refreshBtn.addActionListener(e -> loadInterfaces());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setDividerLocation(260);
        splitPane.setBorder(null);

        // Top: Table
        JPanel tableCard = new JPanel(new BorderLayout(8, 8));
        tableCard.setBackground(UITheme.BG_CARD);
        tableCard.setBorder(UITheme.createCardBorder());

        JLabel tableTitle = new JLabel("DETECTED SYSTEM NETWORK INTERFACES");
        tableTitle.setFont(UITheme.FONT_SUBHEADER);
        tableTitle.setForeground(UITheme.TEXT_MUTED);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Name", "Display Name", "Status", "MAC Address", "Primary IPv4", "Subnet Mask", "MTU (Bytes)", "Loopback"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < currentInterfaces.size()) {
                    showInterfaceDetails(currentInterfaces.get(row));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(tableCard);

        // Bottom: Inspector Box
        JPanel detailsCard = new JPanel(new BorderLayout(8, 8));
        detailsCard.setBackground(UITheme.BG_CARD);
        detailsCard.setBorder(UITheme.createCardBorder());

        JLabel detailsTitle = new JLabel("SELECTED INTERFACE & SUBNET ANALYSIS (COMPUTER NETWORKS CONCEPTS)");
        detailsTitle.setFont(UITheme.FONT_SUBHEADER);
        detailsTitle.setForeground(UITheme.TEXT_MUTED);
        detailsCard.add(detailsTitle, BorderLayout.NORTH);

        detailsArea = new JTextArea();
        detailsArea.setFont(UITheme.FONT_MONO);
        detailsArea.setBackground(new Color(20, 27, 45));
        detailsArea.setForeground(UITheme.TEXT_PRIMARY);
        detailsArea.setEditable(false);
        detailsArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        detailsCard.add(detailsScroll, BorderLayout.CENTER);

        splitPane.setBottomComponent(detailsCard);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadInterfaces() {
        tableModel.setRowCount(0);
        currentInterfaces = ifService.getAllInterfaces();

        for (NetworkInterfaceInfo iface : currentInterfaces) {
            String subnet = iface.getSubnetMasks().isEmpty() ? "N/A" : iface.getSubnetMasks().get(0);
            tableModel.addRow(new Object[]{
                    iface.getName(),
                    iface.getDisplayName(),
                    iface.isUp() ? "UP" : "DOWN",
                    iface.getMacAddress(),
                    iface.getPrimaryIpv4(),
                    subnet,
                    iface.getMtu() > 0 ? iface.getMtu() : "N/A",
                    iface.isLoopback() ? "Yes" : "No"
            });
        }

        if (!currentInterfaces.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            showInterfaceDetails(currentInterfaces.get(0));
        }
    }

    private void showInterfaceDetails(NetworkInterfaceInfo iface) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append(String.format(" INTERFACE SPECIFICATION: %s (%s)\n", iface.getName(), iface.getDisplayName()));
        sb.append("========================================================================\n\n");

        sb.append(String.format("  * Link Status:               %s\n", iface.isUp() ? "UP (Active Link)" : "DOWN (Link Inactive)"));
        sb.append(String.format("  * MAC Hardware Address:      %s\n", iface.getMacAddress()));
        sb.append(String.format("  * Maximum Transmission Unit: %d bytes (Standard Ethernet MTU = 1500)\n", iface.getMtu()));
        sb.append(String.format("  * Interface Capabilities:    Loopback: %s | Point-to-Point: %s | Multicast: %s | Virtual: %s\n\n",
                iface.isLoopback(), iface.isPointToPoint(), iface.supportsMulticast(), iface.isVirtual()));

        sb.append("--- [IP ADDRESS CONFIGURATION] ---\n");
        if (iface.getIpv4Addresses().isEmpty()) {
            sb.append("  (No IPv4 addresses assigned)\n");
        } else {
            for (int i = 0; i < iface.getIpv4Addresses().size(); i++) {
                String ip = iface.getIpv4Addresses().get(i);
                String sub = (i < iface.getSubnetMasks().size()) ? iface.getSubnetMasks().get(i) : "N/A";
                sb.append(String.format("  IPv4 [%d]: %s   Mask: %s\n", i + 1, ip, sub));
            }
        }

        if (!iface.getIpv6Addresses().isEmpty()) {
            sb.append("\n  IPv6 Addresses:\n");
            for (String ip6 : iface.getIpv6Addresses()) {
                sb.append("    -> ").append(ip6).append("\n");
            }
        }

        // Subnet calculations demonstration
        if (!iface.getIpv4Addresses().isEmpty()) {
            String ip = iface.getIpv4Addresses().get(0);
            short prefix = 24; // Default
            if (!iface.getSubnetMasks().isEmpty()) {
                String sm = iface.getSubnetMasks().get(0);
                if (sm.startsWith("/")) {
                    try {
                        prefix = Short.parseShort(sm.substring(1, sm.indexOf(' ')));
                    } catch (Exception ignored) {}
                }
            }

            Map<String, String> subnetCalc = NetworkInterfaceService.calculateSubnetDetails(ip, prefix);
            sb.append("\n--- [COMPUTER NETWORKS: SUBNET & IP MATHEMATICS] ---\n");
            for (Map.Entry<String, String> entry : subnetCalc.entrySet()) {
                sb.append(String.format("  %-22s: %s\n", entry.getKey(), entry.getValue()));
            }
        }

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }
}
