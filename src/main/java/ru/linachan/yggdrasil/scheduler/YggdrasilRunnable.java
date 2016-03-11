package ru.linachan.yggdrasil.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public abstract class YggdrasilRunnable implements Runnable {

    protected YggdrasilCore core;

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilRunnable.class);

    public YggdrasilRunnable(YggdrasilCore core) {
        this.core = core;
    }

    public abstract void run();

    public abstract void onCancel();
}
