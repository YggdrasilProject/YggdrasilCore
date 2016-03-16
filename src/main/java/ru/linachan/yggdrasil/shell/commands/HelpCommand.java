package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HelpCommand extends YggdrasilShellCommand {

    public static String commandName = "help";
    public static String commandDescription = "Print available commands with description";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        for (Map.Entry<Class<? extends YggdrasilShellCommand>, Boolean> commandEntry: commandManager.list().entrySet()) {
            try {
                String commandName = (String) commandEntry.getKey().getField("commandName").get(null);
                String commandDescription = (String) commandEntry.getKey().getField("commandDescription").get(null);

                if (commandName != null) {
                    console.writeLine(String.format("%-20s %s", commandName, (commandDescription != null) ? commandDescription : ""));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error("Unable to get command info", e);
            }
        }
    }

    @Override
    protected void onInterrupt() {}
}
