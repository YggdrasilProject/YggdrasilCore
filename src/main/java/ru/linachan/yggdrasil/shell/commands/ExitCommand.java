package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExitCommand extends YggdrasilShellCommand {

    public static String commandName = "exit";
    public static String commandDescription = "Closes current session";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        exit(0, "logout");
    }
}
