package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;

public class InvalidCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        console.writeLine("Invalid command");
        exit(1);
    }

    @Override
    protected void onInterrupt() {}

}
