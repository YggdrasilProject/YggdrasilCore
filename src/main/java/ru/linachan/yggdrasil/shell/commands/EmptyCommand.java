package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmptyCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {}

    @Override
    protected void onInterrupt() {}
}
