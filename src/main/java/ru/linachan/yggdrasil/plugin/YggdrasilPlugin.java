package ru.linachan.yggdrasil.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public abstract class YggdrasilPlugin {

    protected YggdrasilCore core;

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilPlugin.class);

    public void onPluginInit(YggdrasilCore core) {
        this.core = core;
        onInit();
    }

    protected abstract void onInit();

    protected abstract void onShutdown();

    public void shutdown() {
        onShutdown();
        logger.info(this.getClass().getSimpleName() + " is ready for shutdown");
    }
}
