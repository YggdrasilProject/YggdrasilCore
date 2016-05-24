package ru.linachan.yggdrasil.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public abstract class YggdrasilService implements Runnable {

    protected YggdrasilCore core;
    private Thread serviceThread;
    protected Boolean isRunning;

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilService.class);

    public void onServiceInit(YggdrasilCore core) {
        this.core = core;
        isRunning = true;
        onInit();
    }

    public void setServiceThread(Thread serviceThread) {
        this.serviceThread = serviceThread;
    }

    protected abstract void onInit();

    protected abstract void onShutdown();

    public void stop(Boolean wait) throws InterruptedException {
        onShutdown();
        isRunning = false;

        if (wait) {
            while (serviceThread.isAlive()) {
                Thread.sleep(1000);
            }
            serviceThread.join();
        }

        serviceThread.interrupt();
    }

    public boolean isRunning() {
        return isRunning;
    }
}