package ru.linachan.mmo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.mmo.MMOPlugin;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.Server;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class DataWriter implements Runnable {

    private Server server;
    private final SelectionKey clientKey;
    private final BlockingQueue<byte[]> writeQueue;

    private static Logger logger = LoggerFactory.getLogger(DataWriter.class);
    private boolean isRunning = true;
    private Thread writerThread;

    public DataWriter(SelectionKey sessionKey) {
        server = YggdrasilCore.INSTANCE.getManager(YggdrasilPluginManager.class).get(MMOPlugin.class).getWorldServer();
        clientKey = sessionKey;

        writeQueue = new SynchronousQueue<>();
    }

    public void write(byte[] data) throws InterruptedException {
        writeQueue.put(data);
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                byte[] data = writeQueue.take();

                if (data != null) {
                    server.write(clientKey, data);
                }
            }
        } catch (InterruptedException ignored) {}
    }

    public void start() {
        writerThread = new Thread(this);
        writerThread.start();
    }

    public void stop() {
        isRunning = false;
        writerThread.interrupt();
    }
}
