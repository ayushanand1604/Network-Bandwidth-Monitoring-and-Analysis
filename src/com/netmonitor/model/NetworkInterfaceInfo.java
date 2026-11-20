package com.netmonitor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates properties and state of a physical or logical network interface.
 */
public class NetworkInterfaceInfo {
    private final String name;
    private final String displayName;
    private final String macAddress;
    private final int mtu;
    private final boolean isUp;
    private final boolean isLoopback;
    private final boolean isPointToPoint;
    private final boolean supportsMulticast;
    private final boolean isVirtual;
    private final List<String> ipv4Addresses = new ArrayList<>();
    private final List<String> ipv6Addresses = new ArrayList<>();
    private final List<String> subnetMasks = new ArrayList<>();

    private long rxBytes;
    private long txBytes;
    private long rxPackets;
    private long txPackets;

    public NetworkInterfaceInfo(String name, String displayName, String macAddress, int mtu,
                                boolean isUp, boolean isLoopback, boolean isPointToPoint,
                                boolean supportsMulticast, boolean isVirtual) {
        this.name = name;
        this.displayName = displayName;
        this.macAddress = (macAddress != null && !macAddress.isEmpty()) ? macAddress : "N/A";
        this.mtu = mtu;
        this.isUp = isUp;
        this.isLoopback = isLoopback;
        this.isPointToPoint = isPointToPoint;
        this.supportsMulticast = supportsMulticast;
        this.isVirtual = isVirtual;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getMtu() {
        return mtu;
    }

    public boolean isUp() {
        return isUp;
    }

    public boolean isLoopback() {
        return isLoopback;
    }

    public boolean isPointToPoint() {
        return isPointToPoint;
    }

    public boolean supportsMulticast() {
        return supportsMulticast;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public List<String> getIpv4Addresses() {
        return ipv4Addresses;
    }

    public List<String> getIpv6Addresses() {
        return ipv6Addresses;
    }

    public List<String> getSubnetMasks() {
        return subnetMasks;
    }

    public String getPrimaryIpv4() {
        return ipv4Addresses.isEmpty() ? "No IPv4" : ipv4Addresses.get(0);
    }

    public long getRxBytes() {
        return rxBytes;
    }

    public void setRxBytes(long rxBytes) {
        this.rxBytes = rxBytes;
    }

    public long getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(long txBytes) {
        this.txBytes = txBytes;
    }

    public long getRxPackets() {
        return rxPackets;
    }

    public void setRxPackets(long rxPackets) {
        this.rxPackets = rxPackets;
    }

    public long getTxPackets() {
        return txPackets;
    }

    public void setTxPackets(long txPackets) {
        this.txPackets = txPackets;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s [%s]",
                name, displayName, getPrimaryIpv4(), isUp ? "UP" : "DOWN");
    }
}
