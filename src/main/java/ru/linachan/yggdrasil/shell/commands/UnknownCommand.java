package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UnknownCommand extends YggdrasilShellCommand {

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        output.write(String.format(
            "%s: command not found\r\n", command
        ));
        output.flush();

        exit(1);
    }
}
