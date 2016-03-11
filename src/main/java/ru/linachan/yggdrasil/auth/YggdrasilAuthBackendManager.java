package ru.linachan.yggdrasil.auth;

import ru.linachan.yggdrasil.YggdrasilGenericManager;

public class YggdrasilAuthBackendManager extends YggdrasilGenericManager<YggdrasilAuthBackend> {

    @Override
    protected void onInit() {
        discoverAll();
    }

    @Override
    protected void onDiscover(Class<? extends YggdrasilAuthBackend> discoveredObject) {

    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilAuthBackend> managedObject) {
        managedObjects.get(managedObject).shutdown();
    }

    @Override
    protected void onEnable(Class<? extends YggdrasilAuthBackend> enabledObject) {
        try {
            YggdrasilAuthBackend backendInstance = enabledObject.newInstance();
            backendInstance.setUpBackend(core);
            managedObjects.put(enabledObject, backendInstance);
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate auth backend: {}", e.getMessage());
        }
    }

    @Override
    protected void onDisable(Class<? extends YggdrasilAuthBackend> disabledObject) {
        managedObjects.get(disabledObject).shutdown();
    }

    @Override
    protected void onPackageEnabled(String packageName) {
        discoverAll();
    }

    @Override
    protected void onPackageDisabled(String packageName) {
        cleanup();
    }

    @Override
    public void shutdown() {
        for (Class<? extends YggdrasilAuthBackend> backend: managedObjects.keySet()) {
            managedObjects.get(backend).shutdown();
        }
    }
}
