package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "setenv", description = "Set environment variables")
public class SetEnvCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        if (kwargs.containsKey("USER")) {
            kwargs.remove("USER");
            console.writeLine("USER variable cannot be changed");
        }

        getEnvironment().getEnv().putAll(kwargs);
    }

    @Override
    protected void onInterrupt() {}
}
