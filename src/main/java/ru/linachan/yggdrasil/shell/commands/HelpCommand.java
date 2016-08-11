package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.common.console.tables.Table;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "help", description = "Print available commands with description")
public class HelpCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        Table commandHelp = new Table("Command", "Description");

        commandManager.list().keySet().stream()
            .filter(command -> command.isAnnotationPresent(ShellCommand.class))
            .forEach(command -> {
                ShellCommand commandInfo = command.getAnnotation(ShellCommand.class);
                commandHelp.addRow(commandInfo.command(), commandInfo.description());
            });

        console.writeTable(commandHelp);
    }

    @Override
    protected void onInterrupt() {}
}
