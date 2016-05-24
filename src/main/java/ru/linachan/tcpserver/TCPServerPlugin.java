package ru.linachan.tcpserver;

import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.Plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Plugin(name = "TCPServer", description = "Provides ability to launch TCP-based services")
public class TCPServerPlugin extends YggdrasilPlugin {

    private TCPConnectionManager connectionManager;
    private ThreadGroup threadGroup;

    private Map<Integer, TCPListener> services;

    @Override
    protected void onInit() {
        threadGroup = new ThreadGroup("TCPServer");

        services = new HashMap<>();

        core.registerManager(TCPServiceManager.class);

        connectionManager = new TCPConnectionManager(core, threadGroup, 256);
        connectionManager.start();
    }

    @Override
    protected void onShutdown() {
        core.shutdownManager(TCPServiceManager.class);

        connectionManager.shutdown();
        threadGroup.interrupt();
    }

    public TCPConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void startTCPService(Integer servicePort, TCPService service) {
        if (services.containsKey(servicePort)) {
            throw new IllegalArgumentException("Port " + servicePort + " already in use!");
        }

        try {
            TCPListener serviceListener = new TCPListener(core, threadGroup, servicePort, service);
            services.put(servicePort, serviceListener);
            serviceListener.start();
        } catch (IOException e) {
            logger.error("Unable to start service", e);
        }
    }

    public void stopTCPService(Integer servicePort) {
        if (services.containsKey(servicePort)) {
            TCPListener serviceListener = services.get(servicePort);

            serviceListener.gracefullyShutdown();
            services.remove(servicePort);
        }
    }

    public Map<Integer, String> listTCPServices() {
        Map<Integer, String> serviceMap = new HashMap<>();

        services.entrySet().stream()
            .forEach(service -> serviceMap.put(service.getKey(), service.getValue().getService().getClass().getSimpleName()));

        return serviceMap;
    }
}
