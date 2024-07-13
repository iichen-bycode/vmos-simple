package com.gmspace.app.utils;

import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {


    public static String getDeviceIp() {
        final List<InetAddress> addresses = getAllInetAddresses();
        for (InetAddress addr : addresses) {
            if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                return addr.getHostAddress();
            }
        }
        return "";
    }

    public static List<String> getAllInet4Addresses() {
        return getFilterInetAddresses(Inet4Address.class);
    }

    public static List<String> getAllInet6Addresses() {
        return getFilterInetAddresses(Inet6Address.class);
    }

    private static List<String> getFilterInetAddresses(Class<? extends InetAddress> cls) {
        List<String> ip = new ArrayList<>();
        final List<InetAddress> addresses = getAllInetAddresses();
        for (InetAddress addr : addresses) {
            if (addr.getClass() == cls) {
                final String hostAddress = addr.getHostAddress();
                if (!TextUtils.isEmpty(hostAddress)) {
                    ip.add(hostAddress);
                }
            }
        }
        return ip;
    }

    private static List<InetAddress> getAllInetAddresses() {
        ArrayList<InetAddress> addresses = new ArrayList<>();
        try {
            final Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                final Enumeration<InetAddress> ias = nis.nextElement().getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = (InetAddress) ias.nextElement();
                    addresses.add(ia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
