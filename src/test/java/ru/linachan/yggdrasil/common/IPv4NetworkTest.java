package ru.linachan.yggdrasil.common;

import junit.framework.Assert;
import org.junit.Test;
import ru.linachan.yggdrasil.YggdrasilTestBase;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4NetworkTest extends YggdrasilTestBase {

    @Test
    public void testAcceptValidNetwork() throws UnknownHostException {
        IPv4Network networkFromString = new IPv4Network("127.0.0.0/8");
        IPv4Network networkFromInetAddress = new IPv4Network((Inet4Address) Inet4Address.getByName("192.168.0.0"), 24);

        Assert.assertEquals("127.0.0.0", networkFromString.getNetworkAddress().getHostAddress());
        Assert.assertEquals(Integer.valueOf(8), networkFromString.getNetMask());

        Assert.assertEquals("192.168.0.0", networkFromInetAddress.getNetworkAddress().getHostAddress());
        Assert.assertEquals(Integer.valueOf(24), networkFromInetAddress.getNetMask());
    }

    @Test
    public void testInvalidNetwork() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid network definition");
        new IPv4Network("256.0.0.255/10");
    }

    @Test
    public void testRangeCheck() {
        IPv4Network networkRange = new IPv4Network("172.16.0.0/16");

        Assert.assertTrue(networkRange.inRange("172.16.124.128"));
        Assert.assertFalse(networkRange.inRange("192.168.0.1"));
    }

    @Test
    public void testRandomAddressGenerator() {
        IPv4Network networkRange = new IPv4Network("172.16.0.0/16");

        InetAddress randomAddress = networkRange.getRandomAddress();

        Assert.assertTrue(networkRange.inRange((Inet4Address) randomAddress));
    }
}