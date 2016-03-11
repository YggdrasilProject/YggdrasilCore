package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ShutdownCommand extends YggdrasilShellCommand {

    public static String commandName = "shutdown";
    public static String commandDescription = "Shutdown Yggdrasil";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        output.write("Shutting down Yggdrasil...\r\n");
        output.flush();

        core.shutdown();
        exit(0);
    }

}
