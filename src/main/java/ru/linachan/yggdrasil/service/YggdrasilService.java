package ru.linachan.yggdrasil.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public interface YggdrasilService extends Runnable {

    YggdrasilCore core = YggdrasilCore.INSTANCE;
    Logger logger = LoggerFactory.getLogger(YggdrasilService.class);

    void onInit();
    void onShutdown();

}
