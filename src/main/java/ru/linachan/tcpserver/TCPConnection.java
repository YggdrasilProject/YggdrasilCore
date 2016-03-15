package ru.linachan.tcpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.component.YggdrasilPluginManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPConnection extends Thread {

    private YggdrasilCore core;
    private Socket clientConnection;
    private TCPService service;

    private static Logger logger = LoggerFactory.getLogger(TCPConnection.class);

    public TCPConnection(YggdrasilCore core, Socket clientConnection, TCPService service) {
        this.core = core;
        this.clientConnection = clientConnection;
        this.service = service;
    }

    public void run() {
        try {
            InputStream in = clientConnection.getInputStream();
            OutputStream out = clientConnection.getOutputStream();

            service.handleConnection(core, in, out);

            clientConnection.close();
        } catch (IOException e) {
            logger.error("Unable to process connection", e);
        } finally {
            core.getManager(YggdrasilPluginManager.class)
                .get(TCPServerPlugin.class)
                .getConnectionManager()
                .endConnection();
        }
    }
}
