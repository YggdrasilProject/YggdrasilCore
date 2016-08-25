package ru.linachan.yggdrasil.service;

import ru.linachan.yggdrasil.YggdrasilGenericManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YggdrasilServiceManager extends YggdrasilGenericManager<YggdrasilService> {

    private static final ExecutorService threadPool = Executors.newWorkStealingPool();

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
        threadPool.shutdown();
        for (Class<? extends YggdrasilService> service: managedObjects.keySet()) {
            stopService(service, false);
        }
    }

    private void startService(Class<? extends YggdrasilService> service) {
        try {
            YggdrasilService serviceInstance = service.newInstance();
            serviceInstance.onInit();
            managedObjects.put(service, serviceInstance);
            threadPool.submit(serviceInstance);

            logger.info("Service started: {}", service.getSimpleName());
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to start service", e);
        }
    }

    private void stopService(Class<? extends YggdrasilService> service, Boolean wait) {
        YggdrasilService serviceInstance = managedObjects.get(service);
        serviceInstance.onShutdown();
        logger.info("Service stopped: {}", service.getSimpleName());
    }
}
