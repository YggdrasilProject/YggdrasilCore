package ru.linachan.yggdrasil.shell;

import org.slf4j.Logger;
import org.apache.sshd.common.SshdSocketAddress;
import org.apache.sshd.common.forward.TcpipForwarder;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.session.ConnectionService;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.IOException;

public class YggdrasilShellTCPIP implements TcpipForwarder {

    private YggdrasilCore core;
    private ConnectionService service;

    private static Logger logger = LoggerFactory.getLogger(YggdrasilShellTCPIP.class);

    public YggdrasilShellTCPIP(YggdrasilCore yggdrasilCore, ConnectionService connectionService) {
        core = yggdrasilCore;
        service = connectionService;
    }

    @Override
    public SshdSocketAddress startLocalPortForwarding(SshdSocketAddress sourceAddress, SshdSocketAddress targetAddress) throws IOException {
        logger.info("LocalForwarding: {} -> {}", sourceAddress, targetAddress);

        return null;
    }

    @Override
    public void stopLocalPortForwarding(SshdSocketAddress sshdSocketAddress) throws IOException {

    }

    @Override
    public SshdSocketAddress startRemotePortForwarding(SshdSocketAddress sourceAddress, SshdSocketAddress targetAddress) throws IOException {
        logger.info("RemoteForwarding: {} -> {}", sourceAddress, targetAddress);

        return null;
    }

    @Override
    public void stopRemotePortForwarding(SshdSocketAddress sshdSocketAddress) throws IOException {

    }

    @Override
    public SshdSocketAddress getForwardedPort(int i) {
        return null;
    }

    @Override
    public SshdSocketAddress localPortForwardingRequested(SshdSocketAddress targetAddress) throws IOException {
        logger.info("LocalForwardingRequest: {}", targetAddress);

        return new SshdSocketAddress("127.0.0.1", 8080);
    }

    @Override
    public void localPortForwardingCancelled(SshdSocketAddress sshdSocketAddress) throws IOException {

    }

    @Override
    public SshdSocketAddress startDynamicPortForwarding(SshdSocketAddress targetAddress) throws IOException {
        logger.info("DynamicForwarding: {}", targetAddress);

        return null;
    }

    @Override
    public void stopDynamicPortForwarding(SshdSocketAddress sshdSocketAddress) throws IOException {

    }

    @Override
    public CloseFuture close(boolean b) {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
