package ru.linachan.yggdrasil.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class YggdrasilPlugin {

    protected YggdrasilCore core;
    protected List<Class<? extends YggdrasilPlugin>> dependencies = new ArrayList<>();

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilPlugin.class);

    public static boolean autoStart = false;

    public void onPluginInit(YggdrasilCore core) {
        this.core = core;

        setUpDependencies();
        checkDependencies();
        onInit();

        List<String> dependencyList = dependencies.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.toList());

        logger.info(this.getClass().getSimpleName() + ": " + "initialized");
        logger.info(this.getClass().getSimpleName() + " -> " + dependencyList.toString());
    }

    protected <T extends YggdrasilPlugin> void dependsOn(Class<T> dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    private void checkDependencies() {
        dependencies.stream()
            .filter(dependency -> !core.getManager(YggdrasilPluginManager.class).isEnabled(dependency))
            .forEach(dependency -> core.getManager(YggdrasilPluginManager.class).enable(dependency));
    }

    protected abstract void setUpDependencies();

    protected abstract void onInit();

    protected abstract void onShutdown();

    public void shutdown() {
        onShutdown();
        logger.info(this.getClass().getSimpleName() + " is ready for shutdown");
    }
}
