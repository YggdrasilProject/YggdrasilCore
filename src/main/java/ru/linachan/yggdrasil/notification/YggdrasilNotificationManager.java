package ru.linachan.yggdrasil.notification;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilGenericManager;

public class YggdrasilNotificationManager extends YggdrasilGenericManager<YggdrasilNotificationProvider> {

    private static Logger logger = LoggerFactory.getLogger(YggdrasilNotificationManager.class);

    @Override
    protected void onInit() {
        discoverEnabled();
    }

    @Override
    protected void onDiscover(Class<? extends YggdrasilNotificationProvider> discoveredObject) {
        logger.info("Notification provider discovered: {}", discoveredObject.getSimpleName());
        try {
            YggdrasilNotificationProvider providerInstance = discoveredObject.newInstance();
            managedObjects.put(discoveredObject, providerInstance);
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate notification provider", e);
        }
    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilNotificationProvider> managedObject) {

    }

    @Override
    protected void onEnable(Class<? extends YggdrasilNotificationProvider> enabledObject) {

    }

    @Override
    protected void onDisable(Class<? extends YggdrasilNotificationProvider> disabledObject) {

    }

    @Override
    protected void onPackageEnabled(String packageName) {
        discoverEnabled();
    }

    @Override
    protected void onPackageDisabled(String packageName) {
        cleanup();
    }

    @Override
    public void shutdown() {

    }

    public void sendNotification(YggdrasilNotification notification) {
        for (YggdrasilNotificationProvider provider: managedObjects.values()) {
            provider.sendNotification(notification);
        }
    }
}
