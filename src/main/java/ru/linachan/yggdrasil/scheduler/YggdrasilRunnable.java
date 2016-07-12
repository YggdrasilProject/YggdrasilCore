package ru.linachan.yggdrasil.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public interface YggdrasilRunnable extends Runnable {

    YggdrasilCore core = YggdrasilCore.INSTANCE;
    Logger logger = LoggerFactory.getLogger(YggdrasilRunnable.class);

    void run();
    void onCancel();
}
