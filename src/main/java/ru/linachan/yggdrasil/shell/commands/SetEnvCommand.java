package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SetEnvCommand extends YggdrasilShellCommand {

    public static String commandName = "setenv";
    public static String commandDescription = "Set environment variables";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        if (kwargs.containsKey("USER")) {
            kwargs.remove("USER");
            console.writeLine("USER variable cannot be changed");
        }

        getEnvironment().getEnv().putAll(kwargs);
    }

    @Override
    protected void onInterrupt() {}
}
