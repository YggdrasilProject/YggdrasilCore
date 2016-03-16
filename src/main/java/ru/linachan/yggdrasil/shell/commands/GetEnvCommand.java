package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetEnvCommand extends YggdrasilShellCommand {

    public static String commandName = "getenv";
    public static String commandDescription = "List environment variables";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        console.writeMap(getEnvironment().getEnv(), "variable", "value");
    }

    @Override
    protected void onInterrupt() {}
}
