package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UnknownCommand extends YggdrasilShellCommand {


    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        console.writeLine("%s: command not found", command);

        exit(1);
    }

    @Override
    protected void onInterrupt() {}
}
