package com.netmonitor.service;

import com.netmonitor.model.NetworkInterfaceInfo;

import java.net.*;
import java.util.*;

/**
 * Service to enumerate and inspect network interfaces, IP addresses, MAC addresses,
 * MTUs, and subnet masks using Java Networking APIs (java.net.NetworkInterface, InetAddress, InterfaceAddress).
 */
public class NetworkInterfaceService {

    public List<NetworkInterfaceInfo> getAllInterfaces() {
        List<NetworkInterfaceInfo> list = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return list;

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                String mac = formatMac(ni.getHardwareAddress());
                int mtu = -1;
                try {
                    mtu = ni.getMTU();
                } catch (SocketException ignored) {}

                NetworkInterfaceInfo info = new NetworkInterfaceInfo(
                        ni.getName(),
                        ni.getDisplayName(),
                        mac,
                        mtu,
                        ni.isUp(),
                        ni.isLoopback(),
                        ni.isPointToPoint(),
                        ni.supportsMulticast(),
                        ni.isVirtual()
                );

                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    if (addr instanceof Inet4Address) {
                        info.getIpv4Addresses().add(addr.getHostAddress());
                        short prefix = ia.getNetworkPrefixLength();
                        String mask = prefixToSubnetMask(prefix);
                        info.getSubnetMasks().add("/" + prefix + " (" + mask + ")");
                    } else if (addr instanceof Inet6Address) {
                        // Strip interface index suffix if present (e.g. %en0)
                        String ip6 = addr.getHostAddress();
                        int pct = ip6.indexOf('%');
                        if (pct > 0) ip6 = ip6.substring(0, pct);
                        info.getIpv6Addresses().add(ip6);
                    }
                }
                list.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sort: active non-loopback first, then loopback, then inactive
        list.sort((a, b) -> {
            if (a.isUp() != b.isUp()) return a.isUp() ? -1 : 1;
            if (a.isLoopback() != b.isLoopback()) return a.isLoopback() ? 1 : -1;
            return a.getName().compareTo(b.getName());
        });

        return list;
    }

    public NetworkInterfaceInfo getPrimaryInterface() {
        List<NetworkInterfaceInfo> interfaces = getAllInterfaces();
        for (NetworkInterfaceInfo info : interfaces) {
            if (info.isUp() && !info.isLoopback() && !info.getIpv4Addresses().isEmpty()) {
                return info;
            }
        }
        return interfaces.isEmpty() ? null : interfaces.get(0);
    }

    public static String formatMac(byte[] mac) {
        if (mac == null || mac.length == 0) return "N/A";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    public static String prefixToSubnetMask(short prefix) {
        if (prefix < 0 || prefix > 32) return "N/A";
        int mask = (prefix == 0) ? 0 : 0xFFFFFFFF << (32 - prefix);
        int octet1 = (mask >> 24) & 0xFF;
        int octet2 = (mask >> 16) & 0xFF;
        int octet3 = (mask >> 8) & 0xFF;
        int octet4 = mask & 0xFF;
        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }

    /**
     * Demonstrates Computer Networks Subnet Calculation:
     * Calculates Network Address, Broadcast Address, Usable Host Range, and IP Class.
     */
    public static Map<String, String> calculateSubnetDetails(String ipStr, short prefix) {
        Map<String, String> details = new LinkedHashMap<>();
        try {
            InetAddress inet = InetAddress.getByName(ipStr);
            if (!(inet instanceof Inet4Address)) {
                details.put("Error", "Subnet calculation currently supports IPv4 addresses.");
                return details;
            }
            byte[] ipBytes = inet.getAddress();
            int ipInt = ((ipBytes[0] & 0xFF) << 24) |
                        ((ipBytes[1] & 0xFF) << 16) |
                        ((ipBytes[2] & 0xFF) << 8)  |
                        (ipBytes[3] & 0xFF);

            int mask = (prefix == 0) ? 0 : 0xFFFFFFFF << (32 - prefix);
            int netInt = ipInt & mask;
            int bcastInt = netInt | ~mask;

            String netAddr = intToIp(netInt);
            String bcastAddr = intToIp(bcastInt);
            String subnetMask = prefixToSubnetMask(prefix);

            long totalHosts = (prefix >= 31) ? (32 - prefix == 1 ? 2 : 1) : (1L << (32 - prefix));
            long usableHosts = (prefix >= 31) ? totalHosts : Math.max(0, totalHosts - 2);

            String firstUsable = (usableHosts > 0) ? intToIp(netInt + 1) : "N/A";
            String lastUsable = (usableHosts > 0) ? intToIp(bcastInt - 1) : "N/A";

            int firstOctet = ipBytes[0] & 0xFF;
            String ipClass;
            if (firstOctet >= 1 && firstOctet <= 126) ipClass = "Class A (Default /8)";
            else if (firstOctet == 127) ipClass = "Loopback (127.0.0.0/8)";
            else if (firstOctet >= 128 && firstOctet <= 191) ipClass = "Class B (Default /16)";
            else if (firstOctet >= 192 && firstOctet <= 223) ipClass = "Class C (Default /24)";
            else if (firstOctet >= 224 && firstOctet <= 239) ipClass = "Class D (Multicast)";
            else ipClass = "Class E (Experimental)";

            boolean isPrivate = inet.isSiteLocalAddress();
            boolean isLoopback = inet.isLoopbackAddress();

            details.put("IP Address", ipStr);
            details.put("CIDR Notation", ipStr + "/" + prefix);
            details.put("Subnet Mask", subnetMask);
            details.put("Network Address", netAddr);
            details.put("Broadcast Address", bcastAddr);
            details.put("Usable Host Range", firstUsable + " - " + lastUsable);
            details.put("Total Addresses", String.valueOf(totalHosts));
            details.put("Usable Hosts", String.valueOf(usableHosts));
            details.put("IP Class", ipClass);
            details.put("Address Scope", isLoopback ? "Loopback" : (isPrivate ? "Private (RFC 1918)" : "Public / Internet"));
        } catch (Exception e) {
            details.put("Error", e.getMessage());
        }
        return details;
    }

    private static String intToIp(int ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }
}
