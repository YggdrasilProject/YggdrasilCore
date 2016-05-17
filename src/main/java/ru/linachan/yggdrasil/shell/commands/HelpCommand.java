package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ShellCommand(command = "help", description = "Print available commands with description")
public class HelpCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        Map<String, String> commandHelp = new HashMap<>();

        commandManager.list().keySet().stream()
            .filter(command -> command.isAnnotationPresent(ShellCommand.class))
            .forEach(command -> {
                ShellCommand commandInfo = command.getAnnotation(ShellCommand.class);
                commandHelp.put(commandInfo.command(), commandInfo.description());
            });

        console.writeMap(commandHelp, "Command", "Description");
    }

    @Override
    protected void onInterrupt() {}
}
