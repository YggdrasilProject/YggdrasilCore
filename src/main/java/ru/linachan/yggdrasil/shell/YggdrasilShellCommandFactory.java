package ru.linachan.yggdrasil.shell;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import ru.linachan.yggdrasil.common.console.CommandLineUtils;
import ru.linachan.yggdrasil.shell.commands.InvalidCommand;
import ru.linachan.yggdrasil.shell.commands.UnknownCommand;

public class YggdrasilShellCommandFactory implements CommandFactory {

    private final YggdrasilShellCommandManager commandManager;

    public YggdrasilShellCommandFactory(YggdrasilShellCommandManager yggdrasilShellCommandManager) {
        commandManager = yggdrasilShellCommandManager;
    }

    @Override
    public Command createCommand(String commandLine) {
        YggdrasilShellCommand targetCommand;

        CommandLineUtils.CommandLine command = CommandLineUtils.parse(commandLine);

        targetCommand = routeCommand(command.getCmd());
        targetCommand.setUpCommand(commandManager, command);

        return targetCommand;
    }

    private YggdrasilShellCommand routeCommand(String command) {
        if (command == null) {
            return new InvalidCommand();
        }

        YggdrasilShellCommand commandObject = commandManager.getCommand(command);
        if (commandObject != null) {
            return commandObject;
        }

        return new UnknownCommand();
    }
}
