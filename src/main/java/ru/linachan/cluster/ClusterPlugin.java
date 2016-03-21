package ru.linachan.cluster;

import ru.linachan.yggdrasil.common.IPv4Network;
import ru.linachan.yggdrasil.component.YggdrasilPlugin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class ClusterPlugin extends YggdrasilPlugin {

    private final IPv4Network CLUSTER_NET = new IPv4Network("239.192.0.0/14");

    @Override
    protected void setUpDependencies() {

    }

    @Override
    protected void onInit() {
        try {
            InetAddress clusterAddress = CLUSTER_NET.getRandomAddress();
            NetworkInterface clusterInterface = NetworkInterface.getByName("eno1");

            ClusterClient client1 = new ClusterClient(clusterInterface, 47536, clusterAddress);
            ClusterClient client2 = new ClusterClient(clusterInterface, 47536, clusterAddress);

            client1.start();
            client2.start();

            Thread.sleep(1000);

            ClusterDataPacket testPacket = new ClusterDataPacket(0x00, 0x00, 0x00, 0x00, new byte[100]);
            client1.send(testPacket);

            client1.stop();
            client2.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onShutdown() {

    }
}
