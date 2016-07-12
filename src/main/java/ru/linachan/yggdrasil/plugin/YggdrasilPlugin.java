package ru.linachan.yggdrasil.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public interface YggdrasilPlugin {

    YggdrasilCore core = YggdrasilCore.INSTANCE;
    Logger logger = LoggerFactory.getLogger(YggdrasilPlugin.class);

    void onInit();
    void onShutdown();

}
