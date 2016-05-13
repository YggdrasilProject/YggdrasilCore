package ru.linachan.yggdrasil.plugin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilGenericManager;
import ru.linachan.yggdrasil.plugin.helpers.AutoStart;
import ru.linachan.yggdrasil.plugin.helpers.Dependencies;
import ru.linachan.yggdrasil.plugin.helpers.DependsOn;

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
        if (discoveredObject.isAnnotationPresent(AutoStart.class)) {
            autoStartQueue.add(discoveredObject);
        }
    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilPlugin> managedObject) {
        logger.info("Plugin shutdown: {}", managedObject.getSimpleName());
    }

    @Override
    protected void onEnable(Class<? extends YggdrasilPlugin> enabledObject) {
        logger.info("Plugin enabled: {}", enabledObject.getSimpleName());

        checkDependencies(enabledObject);

        try {
            YggdrasilPlugin pluginInstance = enabledObject.newInstance();
            pluginInstance.onPluginInit(core);
            put(enabledObject, pluginInstance);
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate plugin");
        }
    }

    private void checkDependencies(Class<? extends YggdrasilPlugin> plugin) {
        for (DependsOn dependency : plugin.getAnnotationsByType(DependsOn.class)) {
            logger.info("{} -> {}", plugin.getSimpleName(), dependency.value().getSimpleName());
            if (!isEnabled(dependency.value())) {
                enable(dependency.value());
            }
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
