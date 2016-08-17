package ru.linachan.yggdrasil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.linachan.yggdrasil.auth.YggdrasilAuthBackendManager;
import ru.linachan.yggdrasil.auth.YggdrasilAuthManager;
import ru.linachan.yggdrasil.common.ClassPathHelper;
import ru.linachan.yggdrasil.common.console.CommandLineUtils;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import ru.linachan.yggdrasil.event.YggdrasilEvent;
import ru.linachan.yggdrasil.event.YggdrasilEventSystem;
import ru.linachan.yggdrasil.notification.YggdrasilNotificationManager;
import ru.linachan.yggdrasil.common.Queue;
import ru.linachan.yggdrasil.scheduler.YggdrasilScheduler;
import ru.linachan.yggdrasil.service.YggdrasilServiceManager;
import ru.linachan.yggdrasil.storage.YggdrasilStorage;

public class YggdrasilCore {

    private final List<String> enabledPackages = new ArrayList<>();
    private Reflections discoveryHelper;

    private final YggdrasilConfig config;
    private final CommandLineUtils.CommandLine argv;

    private final YggdrasilEventSystem events;
    private final YggdrasilScheduler scheduler;
    private final YggdrasilStorage storage;

    private YggdrasilAuthManager authManager;

    private final Map<Class<? extends YggdrasilGenericManager<?>>, YggdrasilGenericManager<?>> genericManagers;
    private final Map<String, Queue<?>> queueMap;

    private final Logger logger = LoggerFactory.getLogger(YggdrasilCore.class);

    private boolean isRunning = true;
    private boolean isReadyForShutDown = false;

    public static YggdrasilCore INSTANCE;

    public YggdrasilCore(CommandLineUtils.CommandLine args) throws IOException {
        String configFile = args.getKeywordArgs().getOrDefault("config", "yggdrasil.ini");

        config = YggdrasilConfig.readConfig(new File(configFile));
        argv = args;

        loadPlugins();

        discoveryHelper = new Reflections(
            ClasspathHelper.forPackage("ru.linachan"),
            new SubTypesScanner()
        );

        enabledPackages.add("yggdrasil");

        genericManagers = new HashMap<>();
        queueMap = new HashMap<>();

        events = new YggdrasilEventSystem();
        scheduler = new YggdrasilScheduler();
        storage = new YggdrasilStorage();
    }


    public void setUpManagers() {
        registerManager(YggdrasilAuthBackendManager.class);
        authManager = new YggdrasilAuthManager();

        registerManager(YggdrasilPluginManager.class);
        registerManager(YggdrasilNotificationManager.class);
        registerManager(YggdrasilServiceManager.class);


        registerShutdownHook();
    }

    public void reloadPlugins() {
        loadPlugins();

        discoveryHelper = new Reflections(
            ClasspathHelper.forPackage("ru.linachan"),
            new SubTypesScanner()
        );
    }

    private void loadPlugins() {
        String pluginFolderPath = config.getString("yggdrasil.plugins.path", "plugins");
        File pluginFolder = new File(pluginFolderPath);

        if (pluginFolder.exists()&&pluginFolder.isDirectory()) {
            File rawPluginFolder = new File(pluginFolder, "raw");
            if (rawPluginFolder.exists()&&rawPluginFolder.isDirectory()) {
                try {
                    ClassPathHelper.addFile(rawPluginFolder);
                } catch (IOException e) {
                    logger.error(
                        "Unable to load raw plugins: [{}] {}",
                        e.getClass().getSimpleName(), e.getMessage()
                    );
                }
            }

            File[] pluginFiles = pluginFolder.listFiles();
            if ((pluginFiles != null)&&(pluginFiles.length > 0)) {
                for (File pluginFile: pluginFiles) {
                    try {
                        ClassPathHelper.addFile(pluginFile);
                    } catch (IOException e) {
                        logger.error(
                            "Unable to load plugin {}: [{}] {}",
                            pluginFile.getAbsolutePath(),
                            e.getClass().getSimpleName(), e.getMessage()
                        );
                    }
                }
            }
        }
    }

    public void enablePackage(String packageName) {
        if (!enabledPackages.contains(packageName)) {
            enabledPackages.add(packageName);

            Map<String, Object> data = new HashMap<>();
            data.put("packageName", packageName);

            events.sendEvent(new YggdrasilEvent(
                "packageEnabled", data
            ));

            logger.info("Package enabled: {}", packageName);
        }
    }

    public void disablePackage(String packageName) {
        if (enabledPackages.contains(packageName)) {
            enabledPackages.remove(packageName);

            Map<String, Object> data = new HashMap<>();
            data.put("packageName", packageName);

            events.sendEvent(new YggdrasilEvent(
                "packageDisabled", data
            ));

            logger.info("Package disabled: {}", packageName);
        } else {
            logger.info("Package not enabled: {}", packageName);
        }
    }

    public boolean isPackageEnabled(String packageName) {
        return enabledPackages.contains(packageName);
    }

    public <T> Set<Class<? extends T>> discoverAll(Class<T> parentClass) {
        return discoveryHelper.getSubTypesOf(parentClass);
    }

    public <T> Set<Class<? extends T>> discoverEnabled(Class<T> parentClass) {
        return discoveryHelper.getSubTypesOf(parentClass).stream()
            .filter(discoveredClass -> enabledPackages.contains(discoveredClass.getPackage().getName().split("\\.")[2]))
            .collect(Collectors.toSet());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new YggdrasilShutdownHook()));
    }

    public YggdrasilConfig getConfig() {
        return config;
    }

    public void mainLoop() throws InterruptedException {
        while (isRunning) {
            Thread.sleep(1000);
        }

        logger.info("Yggdrasil main loop finished. Waiting another services to finish...");

        genericManagers.values().stream()
                .collect(Collectors.toList()).forEach(YggdrasilGenericManager::shutdown);

        storage.shutdown();
        scheduler.shutdown();

        isReadyForShutDown = true;

        logger.info("Yggdrasil is down...");
        Runtime.getRuntime().exit(0);
    }

    public void shutdown() {
        isRunning = false;
        logger.info("Shutting down Yggdrasil...");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isReadyForShutDown() {
        return isReadyForShutDown;
    }

    public YggdrasilEventSystem getEventSystem() {
        return events;
    }

    public YggdrasilScheduler getScheduler() {
        return scheduler;
    }

    public YggdrasilStorage getStorage() {
        return storage;
    }

    public YggdrasilAuthManager getAuthManager() {
        return authManager;
    }

    public <T extends YggdrasilGenericManager<?>> void registerManager(Class<T> manager) {
        try {
            YggdrasilGenericManager<?> managerInstance = manager.newInstance();
            genericManagers.put(manager, managerInstance);
            managerInstance.setUpManager();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate manager", e);
        }
    }

    public <T extends YggdrasilGenericManager<?>> void shutdownManager(Class<T> manager) {
        if (genericManagers.containsKey(manager)) {
            genericManagers.get(manager).shutdown();
            genericManagers.remove(manager);
        }
    }

    public <T extends YggdrasilGenericManager<?>> T getManager(Class<T> manager) {
        if (genericManagers.containsKey(manager)) {
            return manager.cast(genericManagers.get(manager));
        }

        return null;
    }

    public <T> boolean createQueue(Class<T> queueType, String queueName) {
        if (!queueMap.containsKey(queueName)) {
            logger.info("Creating queue: [{}] {}", queueType.getSimpleName(), queueName);
            queueMap.put(queueName, new Queue<T>());
            return true;
        }
        return false;
    }

    public Queue<?> getQueue(String queueName) {
        return queueMap.getOrDefault(queueName, null);
    }

    public CommandLineUtils.CommandLine getCommandLineArgs() {
        return argv;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        CommandLineUtils.CommandLine command = CommandLineUtils.parse(String.format(
            "yggdrasil %s", Joiner.on(" ").join(args)
        ));

        YggdrasilCore.INSTANCE = new YggdrasilCore(command);
        YggdrasilCore.INSTANCE.setUpManagers();
        YggdrasilCore.INSTANCE.mainLoop();
    }
}
