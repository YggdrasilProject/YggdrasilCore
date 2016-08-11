package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "shutdown", description = "Shutdown Yggdrasil")
public class ShutdownCommand extends YggdrasilShellCommand {

    protected void init() throws IOException {}

    public void execute() throws IOException {
        console.writeLine("Shutting down Yggdrasil...\r\n");
        core.shutdown();
    }

    @Override
    protected void onInterrupt() {}

}
