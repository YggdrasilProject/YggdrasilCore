package ru.linachan.tcpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.component.YggdrasilPluginManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPListener extends Thread {

    private YggdrasilCore core;
    private ServerSocket serviceSocket;
    private boolean isRunning = true;
    private TCPService service;

    private static Logger logger = LoggerFactory.getLogger(TCPListener.class);

    public TCPListener(
        YggdrasilCore core,
        ThreadGroup threadGroup,
        Integer listenPort,
        TCPService service
    ) throws IOException{
        super(threadGroup, "TCPListener{" + listenPort + "}");
        this.core = core;
        this.service = service;

        serviceSocket = new ServerSocket(listenPort);
        serviceSocket.setSoTimeout(2000);
        serviceSocket.setReuseAddress(true);
    }

    public void gracefullyShutdown() {
        isRunning = false;
        interrupt();
    }

    public void run() {
        while (isRunning) {
            try {
                Socket clientConnection = serviceSocket.accept();

                core.getManager(YggdrasilPluginManager.class)
                    .get(TCPServerPlugin.class)
                    .getConnectionManager()
                    .addConnection(clientConnection, service);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                logger.error("Unable to handle client connection", e);
            }
        }
    }

    public TCPService getService() {
        return service;
    }
}
