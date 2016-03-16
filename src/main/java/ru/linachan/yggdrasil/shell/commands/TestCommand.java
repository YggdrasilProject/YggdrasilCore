package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestCommand extends YggdrasilShellCommand {

    public static String commandName = "test";
    public static String commandDescription = "Test command line parser";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        console.writeLine("Positional Arguments: %s", args);
        console.writeLine("Keyword Arguments:");
        console.writeMap(kwargs);
    }

    @Override
    protected void onInterrupt() {}
}
