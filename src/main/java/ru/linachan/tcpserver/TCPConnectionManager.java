package ru.linachan.tcpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class TCPConnectionManager extends Thread {

    private YggdrasilCore core;
    private Integer maxConnections;
    private Vector<TCPConnection> connections;
    private boolean isRunning;

    private static Logger logger = LoggerFactory.getLogger(TCPConnectionManager.class);

    public TCPConnectionManager(YggdrasilCore core, ThreadGroup threadGroup, Integer maxConnections) {
        super(threadGroup, "TCPConnectionManager");
        setDaemon(true);

        this.core = core;
        this.maxConnections = maxConnections;

        connections = new Vector<>(maxConnections);
    }

    public synchronized void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public synchronized void addConnection(Socket clientConnection, TCPService service) {
        if (connections.size() >= maxConnections) {
            try {
                PrintWriter out = new PrintWriter(clientConnection.getOutputStream());

                out.println("ECONNREFUSE");
                out.flush();

                clientConnection.close();
            } catch (IOException e) {
                logger.error("Unable to handle client connection", e);
            }
        } else {
            TCPConnection connection = new TCPConnection(core, clientConnection, service);
            connections.add(connection);
            connection.start();
        }
    }

    public synchronized void endConnection() {
        notify();
    }

    public void run() {
        while(isRunning) {
            for(int i = 0; i < connections.size(); i++) {
                TCPConnection connection = connections.elementAt(i);
                if (!connection.isAlive()) {
                    connections.removeElementAt(i);
                }
            }

            try {
                synchronized(this) {
                    wait();
                }
            }
            catch(InterruptedException ignored) {}
        }
    }

    public void shutdown() {
        isRunning = false;
    }
}
