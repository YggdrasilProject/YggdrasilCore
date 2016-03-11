package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmptyCommand extends YggdrasilShellCommand {

    @Override
    protected void execute(String command, List<String> strings, Map<String, String> args) throws IOException {
        exit(0);
    }
}
