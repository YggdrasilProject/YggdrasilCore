package ru.linachan.yggdrasil.service;

import ru.linachan.yggdrasil.YggdrasilGenericManager;

public class YggdrasilServiceManager extends YggdrasilGenericManager<YggdrasilService> {

    @Override
    protected void onInit() {
        discoverEnabled();
    }

    @Override
    protected void onDiscover(Class<? extends YggdrasilService> discoveredObject) {
        startService(discoveredObject);
    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilService> managedObject) {
        stopService(managedObject, false);
    }

    @Override
    protected void onEnable(Class<? extends YggdrasilService> enabledObject) {

    }

    @Override
    protected void onDisable(Class<? extends YggdrasilService> disabledObject) {

    }

    @Override
    protected void onPackageEnabled(String packageName) {
        discoverEnabled();
    }

    @Override
    protected void onPackageDisabled(String packageName) {
        cleanup();
    }

    public void shutdown() {
        for (Class<? extends YggdrasilService> service: managedObjects.keySet()) {
            stopService(service, false);
        }
    }

    private void startService(Class<? extends YggdrasilService> service) {
        try {
            YggdrasilService serviceInstance = service.newInstance();
            serviceInstance.onServiceInit(core);
            Thread serviceThread = new Thread(serviceInstance);
            serviceInstance.setServiceThread(serviceThread);
            managedObjects.put(service, serviceInstance);
            serviceThread.start();

            logger.info("Service started: {}", service.getSimpleName());
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to start service", e);
        }
    }

    private void stopService(Class<? extends YggdrasilService> service, Boolean wait) {
        YggdrasilService serviceInstance = managedObjects.get(service);
        try {
            serviceInstance.stop(wait);
            logger.info("Service stopped: {}", service.getSimpleName());
        } catch (InterruptedException e) {
            logger.error("Unable to stop service properly", e);
        }
    }
}
