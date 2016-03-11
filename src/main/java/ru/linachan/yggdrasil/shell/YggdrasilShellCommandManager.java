package ru.linachan.yggdrasil.shell;

import ru.linachan.yggdrasil.YggdrasilGenericManager;

import java.util.ArrayList;
import java.util.List;

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
    protected void onCleanup(Class<? extends YggdrasilShellCommand> aClass) {

    }

    @Override
    protected void onEnable(Class<? extends YggdrasilShellCommand> aClass) {

    }

    @Override
    protected void onDisable(Class<? extends YggdrasilShellCommand> aClass) {

    }

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
                String commandClassName = (String) commandClass.getField("commandName").get("");
                if ((commandClassName!=null)&&(commandClassName.equals(commandName))) {
                    return commandClass.newInstance();
                }
            } catch (NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                logger.error("Unable to instantiate command", e);
            }
        }
        return null;
    }

    public List<String> listCommands() {
        List<String> commandList = new ArrayList<>();

        for (Class<? extends YggdrasilShellCommand> commandClass: managedObjects.keySet()) {
            try {
                String commandName = (String) commandClass.getField("commandName").get("");
                if (commandName != null) {
                    commandList.add(commandName);
                }
            } catch (IllegalAccessException | NoSuchFieldException ignored) {}
        }

        return commandList;
    }
}
