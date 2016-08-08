package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.common.console.tables.Table;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ShellCommand(command = "getenv", description = "List environment variables")
public class GetEnvCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        console.writeTable(new Table(getEnvironment().getEnv(), "variable", "value"));
    }

    @Override
    protected void onInterrupt() {}
}
