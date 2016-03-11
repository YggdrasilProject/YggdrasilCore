package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InvalidCommand extends YggdrasilShellCommand {

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        output.write("Invalid command\r\n");
        output.flush();

        exit(1);
    }

}
