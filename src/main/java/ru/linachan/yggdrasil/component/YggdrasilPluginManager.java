package ru.linachan.yggdrasil.component;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilGenericManager;

import java.util.ArrayList;
import java.util.List;

public class YggdrasilPluginManager extends YggdrasilGenericManager<YggdrasilPlugin> {

    private static Logger logger = LoggerFactory.getLogger(YggdrasilPluginManager.class);
    private List<Class<? extends YggdrasilPlugin>> autoStartQueue = new ArrayList<>();

    @Override
    protected void onInit() {
        discoverAll();
        autoStart();
    }

    @Override
    protected void onDiscover(Class<? extends YggdrasilPlugin> discoveredObject) {
        logger.info("Plugin discovered: {}", discoveredObject.getSimpleName());
        try {
            Boolean autoStart = (Boolean) discoveredObject.getField("autoStart").get(false);
            if (autoStart) {
                autoStartQueue.add(discoveredObject);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Unable to read plugin config", e);
        }
    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilPlugin> managedObject) {
        logger.info("Plugin shutdown: {}", managedObject.getSimpleName());
    }

    @Override
    protected void onEnable(Class<? extends YggdrasilPlugin> enabledObject) {
        logger.info("Plugin enabled: {}", enabledObject.getSimpleName());
        try {
            YggdrasilPlugin pluginInstance = enabledObject.newInstance();
            pluginInstance.onPluginInit(core);
            put(enabledObject, pluginInstance);
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate plugin");
        }
    }

    @Override
    protected void onDisable(Class<? extends YggdrasilPlugin> disabledObject) {
        get(disabledObject).shutdown();
        put(disabledObject, null);
    }

    @Override
    protected void onPackageEnabled(String packageName) {
        discoverAll();
    }

    @Override
    protected void onPackageDisabled(String packageName) {

    }

    @Override
    public void shutdown() {
        managedObjects.values().stream()
            .filter(plugin -> plugin != null)
            .forEach(YggdrasilPlugin::shutdown);
    }

    private void autoStart() {
        for (Class<? extends YggdrasilPlugin> autoStartPlugin: autoStartQueue) {
            logger.info("Auto-starting plugin: {}", autoStartPlugin.getSimpleName());
            enable(autoStartPlugin);
        }

        autoStartQueue.clear();
    }
}
