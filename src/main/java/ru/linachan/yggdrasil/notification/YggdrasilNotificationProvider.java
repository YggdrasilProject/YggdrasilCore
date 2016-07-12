package ru.linachan.yggdrasil.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

public interface YggdrasilNotificationProvider {

    YggdrasilCore core = YggdrasilCore.INSTANCE;
    Logger logger = LoggerFactory.getLogger(YggdrasilNotificationProvider.class);

    void sendNotification(YggdrasilNotification notification);

}
