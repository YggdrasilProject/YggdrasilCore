package ru.linachan.yggdrasil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class YggdrasilShutdownHook implements Runnable {

    private YggdrasilCore core;

    public YggdrasilShutdownHook(YggdrasilCore core) {
        this.core = core;
    }

    private static Logger logger = LoggerFactory.getLogger(YggdrasilShutdownHook.class);

    @Override
    public void run() {
        core.shutdown();

        while (!core.isReadyForShutDown()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Unable to shutdown properly", e);
            }
        }
    }
}
