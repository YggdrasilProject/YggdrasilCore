package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "exit", description = "Closes current session")
public class ExitCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() {
        exit(0, "logout");
    }

    @Override
    protected void onInterrupt() {}
}
