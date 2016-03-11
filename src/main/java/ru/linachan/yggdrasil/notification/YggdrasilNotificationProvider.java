package ru.linachan.yggdrasil.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public abstract class YggdrasilNotificationProvider {

    protected YggdrasilCore core;

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilNotificationProvider.class);

    public void setUp(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
    }

    public abstract void sendNotification(YggdrasilNotification notification);

}
