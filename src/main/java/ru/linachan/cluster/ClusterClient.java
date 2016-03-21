package ru.linachan.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.UUID;

public class ClusterClient implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ClusterClient.class);
    private InetAddress clusterGroup;
    private Integer clusterPort;
    private boolean isRunning = true;

    private DatagramChannel clusterChannel;
    private MembershipKey clusterKey;

    private Thread clientThread;
    private UUID clusterNodeUUID;

    public ClusterClient(NetworkInterface clusterInterface, Integer clusterPort, InetAddress clusterGroup) throws IOException {
        this.clusterGroup = clusterGroup;
        this.clusterPort = clusterPort;
        this.clusterNodeUUID = UUID.randomUUID();

        clusterChannel = DatagramChannel.open(StandardProtocolFamily.INET)
            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
            .bind(new InetSocketAddress(clusterPort))
            .setOption(StandardSocketOptions.IP_MULTICAST_IF, clusterInterface);

        clusterKey = clusterChannel.join(clusterGroup, clusterInterface);
        logger.info("Joined cluster on {}:{}", clusterGroup.getHostAddress(), clusterPort);
    }

    public void start() {
        clientThread = new Thread(this);
        clientThread.start();
    }

    public void stop() {
        isRunning = false;
    }

    public void send(ClusterDataPacket dataPacket) throws IOException {
        dataPacket.setSourceNode(clusterNodeUUID);
        clusterChannel.send(dataPacket.toByteBuffer(), new InetSocketAddress(clusterGroup, clusterPort));
    }

    @Override
    public void run() {
        ByteBuffer channelBuffer = ByteBuffer.allocate(1500);

        while (isRunning) {
            if (clusterKey.isValid()) {
                try {
                    channelBuffer.clear();
                    clusterChannel.receive(channelBuffer);
                    channelBuffer.flip();

                    dispatch(new ClusterDataPacket(channelBuffer));
                } catch (IOException e) {
                    logger.error("Unable to process request: {}", e.getMessage());
                }
            }
        }
    }

    private void dispatch(ClusterDataPacket dataPacket) {
        logger.info("Incoming data from: {} [{}] ({})", dataPacket.getSourceNode(), dataPacket.getOpCode(), dataPacket.getRawData().length);
    }
}
