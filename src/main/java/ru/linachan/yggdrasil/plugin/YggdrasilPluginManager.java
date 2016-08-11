package ru.linachan.yggdrasil.plugin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilGenericManager;
import ru.linachan.yggdrasil.common.SystemInfo;
import ru.linachan.yggdrasil.plugin.helpers.*;

import java.util.ArrayList;
import java.util.List;

public class YggdrasilPluginManager extends YggdrasilGenericManager<YggdrasilPlugin> {

    private static final Logger logger = LoggerFactory.getLogger(YggdrasilPluginManager.class);
    private final List<Class<? extends YggdrasilPlugin>> autoStartQueue = new ArrayList<>();

    @Override
    protected void onInit() {
        discoverAll();
        autoStart();
    }

    @Override
    protected void onDiscover(Class<? extends YggdrasilPlugin> discoveredObject) {
        logger.info("Plugin discovered: {}", getPluginInfo(discoveredObject).name());

        if (discoveredObject.isAnnotationPresent(AutoStart.class)||core.getConfig().getList("yggdrasil.auto_start", String.class).contains(getPluginInfo(discoveredObject).name())) {
            autoStartQueue.add(discoveredObject);
        }
    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilPlugin> managedObject) {
        logger.info("Plugin shutdown: {}", getPluginInfo(managedObject).name());
    }

    @Override
    protected void onEnable(Class<? extends YggdrasilPlugin> enabledObject) {
        if (checkOSDependencies(enabledObject)) {
            if (checkDependencies(enabledObject)) {
                try {
                    YggdrasilPlugin pluginInstance = enabledObject.newInstance();
                    pluginInstance.onInit();
                    put(enabledObject, pluginInstance);

                    logger.info("Plugin enabled: {}", getPluginInfo(enabledObject).name());
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Unable to instantiate plugin {}", getPluginInfo(enabledObject).name());
                }
            } else {
                logger.warn(
                    "Dependency resolution failed for {}",
                    getPluginInfo(enabledObject).name()
                );
                core.disablePackage(enabledObject.getPackage().getName().split("\\.")[2]);
            }
        } else {
            logger.warn(
                "Plugin {} doesn't support {} {} OS. Disabling.",
                getPluginInfo(enabledObject).name(),
                SystemInfo.getOSType(), SystemInfo.getOSArch()
            );
            core.disablePackage(enabledObject.getPackage().getName().split("\\.")[2]);
        }
    }

    private boolean checkOSDependencies(Class<? extends YggdrasilPlugin> plugin) {
        SystemInfo.OSType osType = SystemInfo.getOSType();
        SystemInfo.OSArch osArch = SystemInfo.getOSArch();

        boolean hasDependency = false;
        boolean compatible = false;

        for (OSSupport os : plugin.getAnnotationsByType(OSSupport.class)) {
            hasDependency = true;
            compatible = compatible || ((
                os.arch().equals(SystemInfo.OSArch.ALL) || os.arch().equals(osArch)
            ) && (
                os.value().equals(SystemInfo.OSType.ALL) || os.value().equals(osType)
            ));
        }

        return !hasDependency || compatible;
    }

    private boolean checkDependencies(Class<? extends YggdrasilPlugin> plugin) {
        for (DependsOn dependency : plugin.getAnnotationsByType(DependsOn.class)) {
            logger.info("{} -> {}", getPluginInfo(plugin).name(), getPluginInfo(dependency.value()).name());
            if (!isEnabled(dependency.value()))
                enable(dependency.value());

            if (!isEnabled(dependency.value()))
                return false;
        }

        return true;
    }

    @Override
    protected void onDisable(Class<? extends YggdrasilPlugin> disabledObject) {
        get(disabledObject).onShutdown();
        put(disabledObject, null);
    }

    @Override
    protected void onPackageEnabled(String packageName) {
        discoverAll();
    }

    @Override
    protected void onPackageDisabled(String packageName) {

    }

    public Plugin getPluginInfo(Class<? extends YggdrasilPlugin> pluginClass) {
        if (pluginClass.isAnnotationPresent(Plugin.class)) {
            return pluginClass.getAnnotation(Plugin.class);
        }

        return null;
    }

    public List<String> getPluginDependencies(Class<? extends YggdrasilPlugin> pluginClass) {
        List<String> dependencies = new ArrayList<>();

        for (DependsOn dependency : pluginClass.getAnnotationsByType(DependsOn.class)) {
            if (dependency.value().isAnnotationPresent(Plugin.class)) {
                dependencies.add(dependency.value().getAnnotation(Plugin.class).name());
            }
        }

        return dependencies;
    }

    @Override
    public void shutdown() {
        managedObjects.values().stream()
            .filter(plugin -> plugin != null)
            .forEach(YggdrasilPlugin::onShutdown);
    }

    private void autoStart() {
        for (Class<? extends YggdrasilPlugin> autoStartPlugin: autoStartQueue) {
            logger.info("Auto-starting plugin: {}", getPluginInfo(autoStartPlugin).name());
            enable(autoStartPlugin);
        }

        autoStartQueue.clear();
    }
}
