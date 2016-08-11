package ru.linachan.yggdrasil.shell;

import ru.linachan.yggdrasil.YggdrasilGenericManager;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.util.List;
import java.util.stream.Collectors;

public class YggdrasilShellCommandManager extends YggdrasilGenericManager<YggdrasilShellCommand> {

    @Override
    protected void onInit() {
        discoverEnabled();
    }

    @Override
    protected void onDiscover(Class<? extends YggdrasilShellCommand> aClass) {
        logger.info("Command discovered: {}", aClass.getSimpleName());
    }

    @Override
    protected void onCleanup(Class<? extends YggdrasilShellCommand> aClass) {}

    @Override
    protected void onEnable(Class<? extends YggdrasilShellCommand> aClass) {}

    @Override
    protected void onDisable(Class<? extends YggdrasilShellCommand> aClass) {}

    @Override
    protected void onPackageEnabled(String s) {
        discoverEnabled();
    }

    @Override
    protected void onPackageDisabled(String s) {
        cleanup();
    }

    @Override
    public void shutdown() {

    }

    public YggdrasilShellCommand getCommand(String commandName) {
        for (Class<? extends YggdrasilShellCommand> commandClass: managedObjects.keySet()) {
            try {
                if (commandClass.isAnnotationPresent(ShellCommand.class)) {
                    String commandClassName = commandClass.getAnnotation(ShellCommand.class).command();
                    if (commandClassName.equals(commandName)) {
                        return commandClass.newInstance();
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Unable to instantiate command", e);
            }
        }
        return null;
    }

    public List<String> listCommands() {
        return managedObjects.keySet().stream()
            .filter(commandClass -> commandClass.isAnnotationPresent(ShellCommand.class))
            .map(commandClass -> commandClass.getAnnotation(ShellCommand.class).command())
            .collect(Collectors.toList());
    }
}
