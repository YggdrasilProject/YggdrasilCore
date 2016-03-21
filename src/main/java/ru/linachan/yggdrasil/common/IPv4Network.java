package ru.linachan.yggdrasil.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4Network {

    private Long networkAddress;
    private Integer netMaskBits;

    private Random randomGenerator = new Random();

    private final Pattern NET_DEFINITION_PATTERN = Pattern.compile(
        "^(?<address>([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\.([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\.([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\.([01]?\\d?\\d|2[0-4]\\d|25[0-5]))/(?<cidr>(3[0-2]|[0-2]?\\d))$"
    );

    public IPv4Network(String networkDefinition) {
        Matcher networkDefinitionMatcher = NET_DEFINITION_PATTERN.matcher(networkDefinition);
        if (networkDefinitionMatcher.matches()) {
            networkAddress = ip2long(networkDefinitionMatcher.group("address"));
            netMaskBits = Integer.valueOf(networkDefinitionMatcher.group("cidr"));
        } else {
            throw new IllegalArgumentException("Invalid network definition");
        }
    }

    private static String long2ip(Long number) {
        return ((number >> 24) & 0xFF) + "." + ((number >> 16) & 0xFF) + "." + ((number >> 8) & 0xFF) + "." + (number & 0xFF);
    }

    private static Long ip2long(String address) {
        String[] octets = address.split("\\.");
        long number = 0;

        for (int i = 0; i < octets.length; i++) {
            number += ((Integer.parseInt(octets[i]) % 256 * Math.pow(256, 3 - i)));
        }

        return number;
    }

    public IPv4Network(Inet4Address networkAddress, Integer netMaskBits) {
        this.networkAddress = ip2long(networkAddress.getHostAddress());
        this.netMaskBits = netMaskBits;
    }

    public InetAddress getRandomAddress() {
        long addressOffset = randomGenerator.nextLong() % Math.round(Math.pow(2, 32 - netMaskBits));

        try {
            return InetAddress.getByName(long2ip(networkAddress + addressOffset));
        } catch (UnknownHostException e) {
            return null;
        }
    }
}

