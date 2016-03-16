package ru.linachan.tcpserver;

import ru.linachan.yggdrasil.YggdrasilGenericManager;

public class TCPServiceManager extends YggdrasilGenericManager<TCPService> {

    @Override
    protected void onInit() {
        discoverEnabled();
    }

    @Override
    protected void onDiscover(Class<? extends TCPService> discoveredObject) {

    }

    @Override
    protected void onCleanup(Class<? extends TCPService> managedObject) {

    }

    @Override
    protected void onEnable(Class<? extends TCPService> enabledObject) {

    }

    @Override
    protected void onDisable(Class<? extends TCPService> disabledObject) {

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
}
